package com.rbdip.bookstore.order;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    public OrderController(OrderService orderService, OrderRepository orderRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createOrder(@RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return Map.of("id", order.getId(), "status", order.getStatus());
    }

    @GetMapping("/orders")
    public List<Map<String, Object>> listOrders() {
        Map<Long, Map<String, Object>> ordersById = new LinkedHashMap<>();
        for (OrderLineView line : orderRepository.findOrderLines()) {
            Map<String, Object> order = ordersById.computeIfAbsent(line.orderId(), id -> {
                Map<String, Object> created = new LinkedHashMap<>();
                created.put("id", id);
                created.put("customerFullName", fullName(line.firstName(), line.lastName()));
                created.put("status", line.status());
                created.put("items", new ArrayList<Map<String, Object>>());
                return created;
            });
            if (line.productName() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("items");
                items.add(Map.of("productName", line.productName(), "quantity", line.quantity()));
            }
        }
        return new ArrayList<>(ordersById.values());
    }

    private static String fullName(String firstName, String lastName) {
        if (lastName == null || lastName.isBlank()) {
            return firstName == null ? "" : firstName;
        }
        return firstName == null ? lastName : firstName + " " + lastName;
    }
}
