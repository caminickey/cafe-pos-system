package com.cafe.pos.controller;

import com.cafe.pos.dto.Dtos.AiCartRequest;
import com.cafe.pos.dto.Dtos.AiResponse;
import com.cafe.pos.service.GeminiService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final GeminiService geminiService;

    public AiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/cart-suggestion")
    public AiResponse cartSuggestion(@Valid @RequestBody AiCartRequest request) {
        return new AiResponse(geminiService.suggestAddOn(request.cartItems()));
    }

    @PostMapping("/orders/{orderId}/kitchen-summary")
    public AiResponse kitchenSummary(@PathVariable Long orderId) {
        return new AiResponse(geminiService.kitchenSummary(orderId));
    }
}
