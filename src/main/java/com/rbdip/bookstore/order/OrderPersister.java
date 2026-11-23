package com.rbdip.bookstore.order;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrderPersister {

    private static final String STATUS_NEW = "new";

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderPersister(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public Order persist(CreateOrderRequest request, List<OrderLine> lines) {
        Order order = orderRepository.save(new Order(
                request.customerFullName(), request.customerAddress(), request.customerPhone(), STATUS_NEW));
        for (OrderLine line : lines) {
            orderItemRepository.save(new OrderItem(
                    order.getId(), line.product().getName(), line.product().getPrice(), line.quantity()));
        }
        return order;
    }
}
