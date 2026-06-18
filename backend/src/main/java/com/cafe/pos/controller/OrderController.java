package com.cafe.pos.controller;

import com.cafe.pos.dto.Dtos.CreateOrderRequest;
import com.cafe.pos.dto.Dtos.NoteRequest;
import com.cafe.pos.dto.Dtos.OrderLineRequest;
import com.cafe.pos.dto.Dtos.OrderResponse;
import com.cafe.pos.dto.Dtos.PaymentRequest;
import com.cafe.pos.dto.Dtos.SplitRequest;
import com.cafe.pos.dto.Dtos.SplitResponse;
import com.cafe.pos.dto.Dtos.UpdateLineRequest;
import com.cafe.pos.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/open")
    public List<OrderResponse> openOrders() {
        return orderService.openOrders();
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @PostMapping
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    @PostMapping("/{id}/items")
    public OrderResponse addItem(@PathVariable Long id, @Valid @RequestBody OrderLineRequest request) {
        return orderService.addItem(id, request);
    }

    @PatchMapping("/{orderId}/items/{lineId}")
    public OrderResponse updateLine(@PathVariable Long orderId, @PathVariable Long lineId, @Valid @RequestBody UpdateLineRequest request) {
        return orderService.updateLine(orderId, lineId, request);
    }

    @DeleteMapping("/{orderId}/items/{lineId}")
    public OrderResponse voidLine(@PathVariable Long orderId, @PathVariable Long lineId) {
        return orderService.voidLine(orderId, lineId);
    }

    @PatchMapping("/{id}/note")
    public OrderResponse updateNote(@PathVariable Long id, @RequestBody NoteRequest request) {
        return orderService.updateNote(id, request.note());
    }

    @PostMapping("/{id}/payments")
    public OrderResponse addPayment(@PathVariable Long id, @Valid @RequestBody PaymentRequest request) {
        return orderService.addPayment(id, request);
    }

    @PostMapping("/{id}/split-preview")
    public SplitResponse splitPreview(@PathVariable Long id, @Valid @RequestBody SplitRequest request) {
        return orderService.splitPreview(id, request.people());
    }
}
