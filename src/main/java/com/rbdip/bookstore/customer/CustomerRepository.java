package com.rbdip.bookstore.customer;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("select c from Customer c"
            + " where c.firstName = :#{#lookup.firstName}"
            + " and c.lastName = :#{#lookup.lastName}"
            + " and c.address is not distinct from :#{#lookup.address}"
            + " and c.phone is not distinct from :#{#lookup.phone}")
    List<Customer> findMatching(@Param("lookup") CustomerLookup lookup);
}
