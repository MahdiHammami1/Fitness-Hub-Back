package com.example.demo.repository;

import com.example.demo.model.Order;
import com.example.demo.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    Optional<Order> findByIdAndEmail(String id, String email);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}
