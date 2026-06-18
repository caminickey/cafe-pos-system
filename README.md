# Cafe POS System

A simple but functional cafe point-of-sale system built with Angular, Spring Boot, MySQL, and a real Gemini API integration.

The app supports the daily cafe flow:

- Browse menu items and create a table order.
- Modify an open order mid-meal by adding items, changing modifiers, or voiding a line.
- Save kitchen notes for allergies, milk changes, timing, or special requests.
- Split the remaining bill between friends.
- Record cash, card, or e-wallet payments until the order is fully paid.
- Use Gemini to suggest a cart add-on or convert a complicated order into a kitchen summary.

## Tech Stack

- Frontend: Angular 17
- Backend: Spring Boot 3, Spring Web, Spring Data JPA, Bean Validation
- Database: MySQL 8 with Flyway migrations
- AI: Google Gemini API over a backend HTTP call

## Project Structure

```text
cafe-pos-system/
  backend/           Spring Boot API
  frontend/          Angular cashier UI
  docs/              API and submission documents
  docker-compose.yml MySQL for local development
  .env.example       Required environment variables
```

## Requirements

- JDK 21
- Maven 3.9+
- Node.js 20+
- npm
- Docker Desktop, or a local MySQL 8 server
- Gemini API key

## Environment Variables

Copy `.env.example` and provide real values when running the backend.

```text
DB_URL=jdbc:mysql://localhost:3306/cafe_pos?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USER=root
DB_PASSWORD=password
CORS_ALLOWED_ORIGIN=http://localhost:4200
GEMINI_API_KEY=`GEMINI_API_KEY`
GEMINI_MODEL=gemini-1.5-flash
```

The AI feature is real. If `GEMINI_API_KEY` is missing, the AI endpoints return an error instead of fake content.

## Run Locally

Start MySQL:

```bash
docker compose up -d
```

Start the backend:

```bash
cd backend
mvn spring-boot:run
```

Start the frontend in another terminal:

```bash
cd frontend
npm install
npm start
```

Open `http://localhost:4200`.

## Demo Flow

1. Click menu items to build a new cart.
2. Press `Send order` to create an open table order in MySQL.
3. Add more menu items while the order is open, or update modifiers like `oat milk, no sugar`.
4. Save a kitchen note.
5. Use `Suggest add-on` or `Kitchen summary` to call Gemini through the backend.
6. Use `Split bill` to calculate a per-person amount.
7. Record payments. When the full amount is paid, the backend changes the order status to `PAID`.

## Database

Flyway creates these tables on backend startup:

- `menu_items`
- `cafe_orders`
- `order_lines`
- `payments`

The first migration also seeds a small cafe menu.

## Clean Code Notes

- The backend keeps business rules in `OrderService`.
- Controllers only receive requests and return responses.
- DTO records separate API shapes from JPA entities.
- Voiding an item changes its status instead of deleting history.
- Totals are recalculated on each order change to keep billing predictable.
- Gemini is called only from the backend so the API key is never exposed in Angular.

## Main Endpoints

See `docs/API.md` for a compact endpoint list.
