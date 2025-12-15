package com.example.demo.service;

import com.example.demo.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User create(User user);
    Optional<User> findById(String id);
    List<User> findAll();
    Optional<User> findByEmail(String email);
    User update(String id, User user);
    void delete(String id);
    void deleteAll();
    boolean existsByEmail(String email);
}
