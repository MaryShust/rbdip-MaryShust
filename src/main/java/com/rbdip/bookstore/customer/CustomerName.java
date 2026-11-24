package com.rbdip.bookstore.customer;

/**
 * Разбирает полное имя клиента на first_name/last_name по тому же
 * правилу, что и backfill-миграция ЛР3: первая часть — имя, остаток —
 * фамилия.
 */
public final class CustomerName {

    private CustomerName() {
    }

    public static CustomerNameParts split(String fullName) {
        String normalized = fullName == null ? "" : fullName.trim();
        int space = normalized.indexOf(' ');
        if (space < 0) {
            return new CustomerNameParts(normalized, "");
        }
        return new CustomerNameParts(
                normalized.substring(0, space).trim(),
                normalized.substring(space + 1).trim());
    }

    public record CustomerNameParts(String firstName, String lastName) {
    }
}
