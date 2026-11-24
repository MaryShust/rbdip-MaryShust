package com.rbdip.bookstore.customer;

/**
 * Критерий поиска существующего клиента при оформлении заказа, чтобы не
 * создавать дубликат для того же человека.
 */
public record CustomerLookup(String firstName, String lastName, String address, String phone) {
}
