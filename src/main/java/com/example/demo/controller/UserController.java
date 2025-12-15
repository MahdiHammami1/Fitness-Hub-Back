package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody User user) {
        if (user.getEmail() != null && userService.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already in use");
        }
        User created = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<User> list() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable String id) {
        Optional<User> u = userService.findById(id);
        return u.map(user -> ResponseEntity.ok((Object) user))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body((Object) "User not found"));
    }

    @GetMapping("/by-email")
    public ResponseEntity<Object> getByEmail(@RequestParam String email) {
        Optional<User> u = userService.findByEmail(email);
        return u.map(user -> ResponseEntity.ok((Object) user))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body((Object) "User not found"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody User user) {
        try {
            User updated = userService.update(id, user);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAll() {
        userService.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
