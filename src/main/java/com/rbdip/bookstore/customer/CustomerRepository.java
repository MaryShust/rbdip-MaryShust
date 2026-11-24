package com.rbdip.bookstore.customer;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("select c from Customer c"
            + " where c.fullName = :fullName"
            + " and c.address is not distinct from :address"
            + " and c.phone is not distinct from :phone")
    List<Customer> findMatching(
            @Param("fullName") String fullName,
            @Param("address") String address,
            @Param("phone") String phone);
}
