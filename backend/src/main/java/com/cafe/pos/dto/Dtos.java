package com.cafe.pos.dto;

import com.cafe.pos.model.LineStatus;
import com.cafe.pos.model.OrderStatus;
import com.cafe.pos.model.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Dtos {
    public record MenuItemResponse(Long id, String name, String category, String description, BigDecimal price) {}

    public record OrderLineRequest(@NotNull Long menuItemId, @Min(1) int quantity, String modifiers) {}

    public record CreateOrderRequest(@NotBlank String tableName, @NotEmpty List<@Valid OrderLineRequest> items) {}

    public record UpdateLineRequest(@Min(1) int quantity, String modifiers) {}

    public record NoteRequest(String note) {}

    public record PaymentRequest(
            @NotBlank String payerName,
            @NotNull PaymentMethod method,
            @NotNull @DecimalMin("0.01") BigDecimal amount) {}

    public record SplitRequest(@Min(2) int people) {}

    public record SplitResponse(int people, BigDecimal eachPays, BigDecimal remainingBalance) {}

    public record PaymentResponse(Long id, String payerName, PaymentMethod method, BigDecimal amount, LocalDateTime paidAt) {}

    public record OrderLineResponse(
            Long id,
            Long menuItemId,
            String itemName,
            BigDecimal unitPrice,
            int quantity,
            String modifiers,
            LineStatus status,
            BigDecimal lineTotal) {}

    public record OrderResponse(
            Long id,
            String tableName,
            OrderStatus status,
            String note,
            BigDecimal subtotal,
            BigDecimal serviceTax,
            BigDecimal total,
            BigDecimal paidAmount,
            BigDecimal balanceDue,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<OrderLineResponse> lines,
            List<PaymentResponse> payments) {}

    public record AiCartRequest(List<String> cartItems) {}

    public record AiResponse(String text) {}
}
