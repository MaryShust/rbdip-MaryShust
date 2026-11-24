package com.rbdip.bookstore.order;

/**
 * Плоская проекция строки заказа (заказ + клиент + позиция + товар),
 * получаемая одним JOIN-запросом. Используется в GET /orders, чтобы
 * избежать N+1: все данные приходят за один SQL-запрос.
 */
public record OrderLineView(
        Long orderId,
        String firstName,
        String lastName,
        String status,
        String productName,
        Integer quantity) {
}
