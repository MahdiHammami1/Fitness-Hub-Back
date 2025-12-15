package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User create(User user) {
        Instant now = Instant.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        if (user.getEnabled() == null) {
            user.setEnabled(true);
        }
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User update(String id, User user) {
        return userRepository.findById(id).map(existing -> {
            if (user.getEmail() != null) existing.setEmail(user.getEmail());
            if (user.getPasswordHash() != null) existing.setPasswordHash(user.getPasswordHash());
            if (user.getRole() != null) existing.setRole(user.getRole());
            if (user.getFullName() != null) existing.setFullName(user.getFullName());
            if (user.getPhone() != null) existing.setPhone(user.getPhone());
            if (user.getEnabled() != null) existing.setEnabled(user.getEnabled());
            if (user.getTermsAcceptedAt() != null) existing.setTermsAcceptedAt(user.getTermsAcceptedAt());
            existing.setUpdatedAt(Instant.now());
            return userRepository.save(existing);
        }).orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    @Override
    public void delete(String id) {
        userRepository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        userRepository.deleteAll();
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
