package com.rbdip.bookstore.order;

import com.rbdip.bookstore.product.Product;
import com.rbdip.bookstore.product.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrderLineAssembler {

    private static final int DEFAULT_QUANTITY = 1;

    private final ProductRepository productRepository;

    public OrderLineAssembler(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<OrderLine> assemble(CreateOrderRequest request) {
        List<OrderLine> lines = new ArrayList<>();
        for (CreateOrderRequest.Item item : request.items()) {
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "product " + item.productId() + " not found"));
            lines.add(new OrderLine(product, resolveQuantity(item.quantity())));
        }
        return lines;
    }

    private int resolveQuantity(Integer requestedQuantity) {
        int quantity = requestedQuantity == null ? DEFAULT_QUANTITY : requestedQuantity;
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        return quantity;
    }
}
