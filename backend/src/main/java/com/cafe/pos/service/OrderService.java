package com.cafe.pos.service;

import com.cafe.pos.dto.Dtos.CreateOrderRequest;
import com.cafe.pos.dto.Dtos.OrderLineRequest;
import com.cafe.pos.dto.Dtos.OrderResponse;
import com.cafe.pos.dto.Dtos.PaymentRequest;
import com.cafe.pos.dto.Dtos.SplitResponse;
import com.cafe.pos.dto.Dtos.UpdateLineRequest;
import com.cafe.pos.model.CafeOrder;
import com.cafe.pos.model.LineStatus;
import com.cafe.pos.model.MenuItem;
import com.cafe.pos.model.OrderLine;
import com.cafe.pos.model.OrderStatus;
import com.cafe.pos.model.Payment;
import com.cafe.pos.repository.MenuItemRepository;
import com.cafe.pos.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private static final BigDecimal SERVICE_TAX_RATE = new BigDecimal("0.06");

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderMapper mapper;

    public OrderService(OrderRepository orderRepository, MenuItemRepository menuItemRepository, OrderMapper mapper) {
        this.orderRepository = orderRepository;
        this.menuItemRepository = menuItemRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> openOrders() {
        return orderRepository.findByStatusOrderByUpdatedAtDesc(OrderStatus.OPEN).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        return mapper.toResponse(findOrder(id));
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        CafeOrder order = new CafeOrder();
        order.setTableName(request.tableName());
        request.items().forEach(item -> addLine(order, item));
        recalculate(order);
        return mapper.toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse addItem(Long orderId, OrderLineRequest request) {
        CafeOrder order = findEditableOrder(orderId);
        addLine(order, request);
        recalculate(order);
        return mapper.toResponse(order);
    }

    @Transactional
    public OrderResponse updateLine(Long orderId, Long lineId, UpdateLineRequest request) {
        CafeOrder order = findEditableOrder(orderId);
        OrderLine line = findLine(order, lineId);
        line.setQuantity(request.quantity());
        line.setModifiers(request.modifiers());
        recalculate(order);
        return mapper.toResponse(order);
    }

    @Transactional
    public OrderResponse voidLine(Long orderId, Long lineId) {
        CafeOrder order = findEditableOrder(orderId);
        findLine(order, lineId).setStatus(LineStatus.VOIDED);
        recalculate(order);
        return mapper.toResponse(order);
    }

    @Transactional
    public OrderResponse updateNote(Long orderId, String note) {
        CafeOrder order = findEditableOrder(orderId);
        order.setNote(note);
        order.touch();
        return mapper.toResponse(order);
    }

    @Transactional
    public OrderResponse addPayment(Long orderId, PaymentRequest request) {
        CafeOrder order = findEditableOrder(orderId);
        BigDecimal balance = order.getTotal().subtract(order.getPaidAmount());
        if (request.amount().compareTo(balance) > 0) {
            throw new IllegalArgumentException("Payment cannot be larger than the remaining balance.");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPayerName(request.payerName());
        payment.setMethod(request.method());
        payment.setAmount(request.amount().setScale(2, RoundingMode.HALF_UP));
        order.getPayments().add(payment);

        order.setPaidAmount(order.getPaidAmount().add(payment.getAmount()).setScale(2, RoundingMode.HALF_UP));
        if (order.getPaidAmount().compareTo(order.getTotal()) >= 0) {
            order.setStatus(OrderStatus.PAID);
        }
        order.touch();
        return mapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public SplitResponse splitPreview(Long orderId, int people) {
        CafeOrder order = findOrder(orderId);
        BigDecimal remaining = order.getTotal().subtract(order.getPaidAmount()).max(BigDecimal.ZERO);
        BigDecimal each = remaining.divide(BigDecimal.valueOf(people), 2, RoundingMode.HALF_UP);
        return new SplitResponse(people, each, remaining);
    }

    private void addLine(CafeOrder order, OrderLineRequest request) {
        MenuItem item = menuItemRepository.findById(request.menuItemId())
                .filter(MenuItem::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Menu item not found: " + request.menuItemId()));

        OrderLine line = new OrderLine();
        line.setOrder(order);
        line.setMenuItem(item);
        line.setItemName(item.getName());
        line.setUnitPrice(item.getPrice());
        line.setQuantity(request.quantity());
        line.setModifiers(request.modifiers());
        order.getLines().add(line);
    }

    private void recalculate(CafeOrder order) {
        BigDecimal subtotal = order.getLines().stream()
                .filter(line -> line.getStatus() == LineStatus.ACTIVE)
                .map(line -> line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal serviceTax = subtotal.multiply(SERVICE_TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        order.setSubtotal(subtotal);
        order.setServiceTax(serviceTax);
        order.setTotal(subtotal.add(serviceTax).setScale(2, RoundingMode.HALF_UP));
        order.touch();
    }

    private CafeOrder findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
    }

    private CafeOrder findEditableOrder(Long orderId) {
        CafeOrder order = findOrder(orderId);
        if (order.getStatus() != OrderStatus.OPEN) {
            throw new IllegalStateException("Only open orders can be changed.");
        }
        return order;
    }

    private OrderLine findLine(CafeOrder order, Long lineId) {
        return order.getLines().stream()
                .filter(line -> line.getId().equals(lineId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Order line not found: " + lineId));
    }
}
