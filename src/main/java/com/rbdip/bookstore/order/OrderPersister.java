package com.rbdip.bookstore.order;

import com.rbdip.bookstore.customer.Customer;
import com.rbdip.bookstore.customer.CustomerLookup;
import com.rbdip.bookstore.customer.CustomerName;
import com.rbdip.bookstore.customer.CustomerName.CustomerNameParts;
import com.rbdip.bookstore.customer.CustomerRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrderPersister {

    private static final String STATUS_NEW = "new";

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;

    public OrderPersister(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerRepository = customerRepository;
    }

    public Order persist(CreateOrderRequest request, List<OrderLine> lines) {
        Customer customer = resolveCustomer(request);
        Order order = orderRepository.save(new Order(customer.getId(), STATUS_NEW));
        for (OrderLine line : lines) {
            orderItemRepository.save(new OrderItem(
                    order.getId(), line.product().getId(), line.quantity()));
        }
        return order;
    }

    private Customer resolveCustomer(CreateOrderRequest request) {
        CustomerNameParts name = CustomerName.split(request.customerFullName());
        CustomerLookup lookup = new CustomerLookup(
                name.firstName(), name.lastName(), request.customerAddress(), request.customerPhone());
        List<Customer> existing = customerRepository.findMatching(lookup);
        if (!existing.isEmpty()) {
            return existing.get(0);
        }
        return customerRepository.save(new Customer(
                lookup.firstName(), lookup.lastName(), lookup.address(), lookup.phone()));
    }
}
