package com.cafe.pos.service;

import com.cafe.pos.dto.Dtos.OrderLineResponse;
import com.cafe.pos.dto.Dtos.OrderResponse;
import com.cafe.pos.dto.Dtos.PaymentResponse;
import com.cafe.pos.model.CafeOrder;
import com.cafe.pos.model.LineStatus;
import com.cafe.pos.model.OrderLine;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {
    public OrderResponse toResponse(CafeOrder order) {
        List<OrderLineResponse> lines = order.getLines().stream().map(this::toLineResponse).toList();
        List<PaymentResponse> payments = order.getPayments().stream()
                .map(payment -> new PaymentResponse(payment.getId(), payment.getPayerName(), payment.getMethod(), payment.getAmount(), payment.getPaidAt()))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getTableName(),
                order.getStatus(),
                order.getNote(),
                order.getSubtotal(),
                order.getServiceTax(),
                order.getTotal(),
                order.getPaidAmount(),
                order.getTotal().subtract(order.getPaidAmount()).max(BigDecimal.ZERO),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                lines,
                payments);
    }

    private OrderLineResponse toLineResponse(OrderLine line) {
        BigDecimal lineTotal = line.getStatus() == LineStatus.ACTIVE
                ? line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity()))
                : BigDecimal.ZERO;
        return new OrderLineResponse(
                line.getId(),
                line.getMenuItem().getId(),
                line.getItemName(),
                line.getUnitPrice(),
                line.getQuantity(),
                line.getModifiers(),
                line.getStatus(),
                lineTotal);
    }
}
