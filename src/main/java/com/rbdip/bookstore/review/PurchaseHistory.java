package com.rbdip.bookstore.review;

/**
 * Порт (Strangler Fig): контракт, через который модуль review узнаёт,
 * покупал ли клиент товар. Реализация живёт на стороне order, поэтому
 * review больше не зависит от внутренних классов/репозиториев order.
 */
public interface PurchaseHistory {

    boolean hasPurchased(Long productId);
}
