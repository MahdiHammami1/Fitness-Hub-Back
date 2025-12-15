package com.example.demo.service;

import com.example.demo.DTO.CreateOrderRequest;
import com.example.demo.model.Order;

public interface ShopService {
    Order createOrder(CreateOrderRequest req);
}

