package com.rbdip.bookstore.order;

import com.rbdip.bookstore.customer.Customer;
import com.rbdip.bookstore.customer.CustomerRepository;
import com.rbdip.bookstore.product.Product;
import com.rbdip.bookstore.product.ProductRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public OrderController(
            OrderService orderService,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createOrder(@RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return Map.of("id", order.getId(), "status", order.getStatus());
    }

    @GetMapping("/orders")
    public List<Map<String, Object>> listOrders() {
        Map<Long, Customer> customersById = customerRepository.findAll().stream()
                .collect(Collectors.toMap(Customer::getId, Function.identity()));
        Map<Long, Product> productsById = productRepository.findAll().stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(order -> {
                    // N+1: отдельный запрос на позиции для каждого заказа вместо
                    // одного JOIN FETCH / batch-запроса. Цель для ЛР4.
                    List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
                    Customer customer = customersById.get(order.getCustomerId());
                    return Map.<String, Object>of(
                            "id", order.getId(),
                            "customerFullName", customer == null ? "" : customer.getFullName(),
                            "status", order.getStatus(),
                            "items", items.stream()
                                    .map(i -> {
                                        Product product = productsById.get(i.getProductId());
                                        return Map.of(
                                                "productName", product == null ? "" : product.getName(),
                                                "quantity", i.getQuantity());
                                    })
                                    .toList());
                })
                .toList();
    }
}
