-- ЛР2: нормализация схемы.
-- 1. Контактные данные клиента выносятся из orders в отдельную таблицу
--    customers (имя клиента - в колонке full_name).
-- 2. order_items перестаёт дублировать название/цену товара и ссылается
--    на products по внешнему ключу product_id.

CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    phone VARCHAR(50)
);

-- Переносим уже существующих клиентов (уникальные комбинации данных).
INSERT INTO customers (full_name, address, phone)
SELECT DISTINCT customer_full_name, customer_address, customer_phone
FROM orders;

ALTER TABLE orders ADD COLUMN customer_id BIGINT;

UPDATE orders o
SET customer_id = c.id
FROM customers c
WHERE c.full_name = o.customer_full_name
  AND c.address IS NOT DISTINCT FROM o.customer_address
  AND c.phone IS NOT DISTINCT FROM o.customer_phone;

ALTER TABLE orders ALTER COLUMN customer_id SET NOT NULL;

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers (id);

ALTER TABLE orders
    DROP COLUMN customer_full_name,
    DROP COLUMN customer_address,
    DROP COLUMN customer_phone;

-- Восстанавливаем недостающие товары, чтобы не потерять исторические позиции.
INSERT INTO products (name, price)
SELECT DISTINCT oi.product_name, oi.product_price
FROM order_items oi
WHERE NOT EXISTS (
    SELECT 1
    FROM products p
    WHERE p.name = oi.product_name
      AND p.price = oi.product_price
);

ALTER TABLE order_items ADD COLUMN product_id BIGINT;

UPDATE order_items oi
SET product_id = p.id
FROM products p
WHERE oi.product_id IS NULL
  AND p.name = oi.product_name
  AND p.price = oi.product_price;

ALTER TABLE order_items ALTER COLUMN product_id SET NOT NULL;

ALTER TABLE order_items
    ADD CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id);

ALTER TABLE order_items
    DROP COLUMN product_name,
    DROP COLUMN product_price;
