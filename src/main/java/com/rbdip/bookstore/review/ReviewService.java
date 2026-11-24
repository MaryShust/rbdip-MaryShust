package com.rbdip.bookstore.review;

import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Модуль review больше не зависит от внутренних классов пакета order:
 * проверка «покупал ли клиент товар» идёт через порт PurchaseHistory,
 * реализуемый на стороне order (паттерн Strangler Fig).
 */
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PurchaseHistory purchaseHistory;

    public ReviewService(ReviewRepository reviewRepository, PurchaseHistory purchaseHistory) {
        this.reviewRepository = reviewRepository;
        this.purchaseHistory = purchaseHistory;
    }

    public Review addReview(Long productId, String authorName, Integer rating, String comment) {
        // Проверка покупки через порт: в учебном поведении отзыв создаётся
        // в любом случае, но модуль review остаётся независим от order.
        purchaseHistory.hasPurchased(productId);
        Review review = new Review(productId, authorName == null ? "anonymous" : authorName, rating, comment);
        return reviewRepository.save(review);
    }

    public List<Review> listReviews(Long productId) {
        return reviewRepository.findByProductId(productId);
    }
}
