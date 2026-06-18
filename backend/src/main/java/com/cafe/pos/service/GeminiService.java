package com.cafe.pos.service;

import com.cafe.pos.dto.Dtos.OrderResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OrderService orderService;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    public GeminiService(OrderService orderService) {
        this.orderService = orderService;
    }

    public String suggestAddOn(List<String> cartItems) {
        String prompt = """
                You are helping a cafe cashier. Based on this cart, suggest one useful add-on item from a normal cafe menu.
                Keep it under 45 words and mention why it fits.
                Cart: %s
                """.formatted(String.join(", ", cartItems));
        return askGemini(prompt);
    }

    public String kitchenSummary(Long orderId) {
        OrderResponse order = orderService.getOrder(orderId);
        String lineText = order.lines().stream()
                .filter(line -> line.status().name().equals("ACTIVE"))
                .map(line -> "%dx %s (%s)".formatted(line.quantity(), line.itemName(), line.modifiers() == null ? "no modifiers" : line.modifiers()))
                .toList()
                .toString();
        String prompt = """
                Convert this cafe order into a clear kitchen note. Highlight allergies, milk changes, no-sugar requests,
                timing issues, and confusing modifiers. Do not invent items.
                Table: %s
                Order note: %s
                Items: %s
                """.formatted(order.tableName(), order.note(), lineText);
        return askGemini(prompt);
    }

    private String askGemini(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is not set.");
        }
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "contents", List.of(Map.of(
                            "role", "user",
                            "parts", List.of(Map.of("text", prompt))))));
            URI uri = URI.create("https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey);
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Gemini request failed with status " + response.statusCode());
            }
            JsonNode root = objectMapper.readTree(response.body());
            return root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("No AI response returned.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Gemini request interrupted.", ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to call Gemini: " + ex.getMessage(), ex);
        }
    }
}
