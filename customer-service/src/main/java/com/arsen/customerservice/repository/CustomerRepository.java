package com.arsen.customerservice.repository;

import com.arsen.customerservice.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    Optional<Customer> findByAuthUserId(String authUserId);

    Optional<Customer> findByCustomerNumber(String customerNumber);

    boolean existsByAuthUserId(String authUserId);

    boolean existsByCustomerNumber(String customerNumber);
}
