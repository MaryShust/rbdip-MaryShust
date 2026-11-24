package com.rbdip.bookstore.order;

import com.rbdip.bookstore.review.PurchaseHistory;
import org.springframework.stereotype.Component;

/**
 * Адаптер (Strangler Fig): реализует порт review, инкапсулируя знание о
 * внутренних репозиториях order. Сам модуль review обращается только к
 * интерфейсу PurchaseHistory, не зная про order.
 */
@Component
public class OrderPurchaseHistory implements PurchaseHistory {

    private final OrderItemRepository orderItemRepository;

    public OrderPurchaseHistory(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public boolean hasPurchased(Long productId) {
        return orderItemRepository.existsByProductId(productId);
    }
}
