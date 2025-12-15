package com.example.demo.service;

import com.example.demo.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    Order create(Order order);

    Order update(String id, Order order);

    Optional<Order> getById(String id);

    List<Order> getAll();

    void delete(String id);

    void deleteAll();
}
