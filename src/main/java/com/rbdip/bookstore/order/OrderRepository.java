package com.rbdip.bookstore.order;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("select new com.rbdip.bookstore.order.OrderLineView("
            + " o.id, c.firstName, c.lastName, o.status, p.name, i.quantity)"
            + " from Order o"
            + " join Customer c on c.id = o.customerId"
            + " left join OrderItem i on i.orderId = o.id"
            + " left join Product p on p.id = i.productId"
            + " order by o.id, i.id")
    List<OrderLineView> findOrderLines();
}
