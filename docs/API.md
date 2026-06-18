# API Quick Reference

Base URL: `http://localhost:8080/api`

## Menu

- `GET /menu-items` - active seeded cafe menu.

## Orders

- `GET /orders/open` - open tables.
- `GET /orders/{id}` - one order with lines, totals, and payments.
- `POST /orders` - create order.
- `POST /orders/{id}/items` - add an item while the meal is still open.
- `PATCH /orders/{orderId}/items/{lineId}` - change quantity or modifiers.
- `DELETE /orders/{orderId}/items/{lineId}` - void an item without erasing order history.
- `PATCH /orders/{id}/note` - update kitchen note.
- `POST /orders/{id}/split-preview` - calculate equal split against remaining balance.
- `POST /orders/{id}/payments` - record payment. Order becomes `PAID` when paid amount reaches total.

## AI

- `POST /ai/cart-suggestion` - sends cart item names to Gemini and returns one add-on suggestion.
- `POST /ai/orders/{orderId}/kitchen-summary` - sends the saved order to Gemini and returns a kitchen-friendly summary.
