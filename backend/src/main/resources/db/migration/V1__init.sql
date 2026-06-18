CREATE TABLE menu_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(60) NOT NULL,
    description VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE cafe_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    table_name VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL,
    note VARCHAR(500),
    subtotal DECIMAL(10,2) NOT NULL,
    service_tax DECIMAL(10,2) NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    paid_amount DECIMAL(10,2) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
);

CREATE TABLE order_lines (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    menu_item_id BIGINT NOT NULL,
    item_name VARCHAR(120) NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL,
    modifiers VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_order_lines_order FOREIGN KEY (order_id) REFERENCES cafe_orders(id),
    CONSTRAINT fk_order_lines_menu_item FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);

CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    payer_name VARCHAR(80) NOT NULL,
    method VARCHAR(30) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    paid_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES cafe_orders(id)
);

INSERT INTO menu_items (name, category, description, price, active) VALUES
('Latte', 'Coffee', 'Espresso with steamed milk', 11.00, TRUE),
('Iced Americano', 'Coffee', 'Cold espresso over water and ice', 9.00, TRUE),
('Cappuccino', 'Coffee', 'Espresso, milk and thick foam', 10.50, TRUE),
('Matcha Latte', 'Tea', 'Japanese matcha with milk', 12.00, TRUE),
('Lemon Iced Tea', 'Tea', 'Black tea, lemon and syrup', 7.50, TRUE),
('Butter Croissant', 'Pastry', 'Flaky butter pastry', 8.00, TRUE),
('Chicken Sandwich', 'Food', 'Toasted sandwich with chicken and greens', 16.00, TRUE),
('Mushroom Pasta', 'Food', 'Creamy mushroom pasta', 19.00, TRUE),
('Chocolate Cake', 'Dessert', 'Rich slice of chocolate cake', 13.00, TRUE),
('Sea Salt Fries', 'Side', 'Crispy fries with sea salt', 9.50, TRUE);
