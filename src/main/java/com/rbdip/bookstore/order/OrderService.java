package com.rbdip.bookstore.order;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final String DEFAULT_CUSTOMER_TYPE = "regular";

    private final OrderRequestValidator validator;
    private final OrderLineAssembler lineAssembler;
    private final OrderPersister persister;
    private final OrderNotifier notifier;
    private final PricingCalculator pricingCalculator = new PricingCalculator();

    public OrderService(
            OrderRequestValidator validator,
            OrderLineAssembler lineAssembler,
            OrderPersister persister,
            OrderNotifier notifier) {
        this.validator = validator;
        this.lineAssembler = lineAssembler;
        this.persister = persister;
        this.notifier = notifier;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        validator.validate(request);
        List<OrderLine> lines = lineAssembler.assemble(request);
        BigDecimal total = pricingCalculator.calculateOrderTotal(
                toPricingItems(lines), customerType(request), request.couponCode());
        Order order = persister.persist(request, lines);
        notifier.orderPlaced(order, total);
        return order;
    }

    private List<PricingCalculator.LineItem> toPricingItems(List<OrderLine> lines) {
        return lines.stream()
                .map(line -> new PricingCalculator.LineItem(line.product().getPrice(), line.quantity()))
                .toList();
    }

    private String customerType(CreateOrderRequest request) {
        return request.customerType() == null ? DEFAULT_CUSTOMER_TYPE : request.customerType();
    }
}
