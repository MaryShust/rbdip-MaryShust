package com.rbdip.bookstore.order;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailOrderNotifier implements OrderNotifier {

    private static final Logger LOG = LoggerFactory.getLogger(EmailOrderNotifier.class);

    @Override
    public void orderPlaced(Order order, BigDecimal total) {
        if (LOG.isInfoEnabled()) {
            LOG.info("[email] Dear {}, your order #{} for {} has been placed.",
                    order.getCustomerFullName(), order.getId(), total);
        }
    }
}
