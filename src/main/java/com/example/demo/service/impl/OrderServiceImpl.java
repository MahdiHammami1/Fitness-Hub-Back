package com.example.demo.service.impl;

import com.example.demo.model.Order;
import com.example.demo.model.OrderStatus;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.service.EmailService;
import com.example.demo.service.EmailTemplates;
import com.example.demo.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository repository;
    private final OrderItemRepository itemRepo;
    private final EmailService emailService;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Override
    public Order create(Order order) {
        if (order.getStatus() == null) order.setStatus(OrderStatus.PENDING);
        Order saved = repository.save(order);

        // fetch items for this order (to include in the email table)
        List<com.example.demo.model.OrderItem> items = itemRepo.findByOrderId(saved.getId());
        log.debug("Order {} contains {} items (will be embedded in email)", saved.getId(), items == null ? 0 : items.size());

        // Attempt to send confirmation email to customer (with products table)
        try {
            String to = saved.getEmail();
            if (to != null && !to.isBlank()) {
                String subject = "Confirmation de commande - Wouhouch Hub";
                String html = EmailTemplates.orderConfirmation(
                        saved.getCustomerName(),
                        saved.getId(),
                        saved.getTotal() == null ? "0" : saved.getTotal().toString(),
                        saved.getStatus() == null ? "" : saved.getStatus().name(),
                        items
                );
                log.info("Sending order confirmation email to {} (order={})", to, saved.getId());
                emailService.sendHtmlMessage(to, subject, html);
            } else {
                log.warn("Order saved but no customer email provided (order={})", saved.getId());
            }
        } catch (Exception ex) {
            log.error("Failed sending confirmation email for order {}: {}", saved.getId(), ex.getMessage(), ex);
        }

        // Attempt to send admin notification (including products table)
        try {
            if (adminEmail != null && !adminEmail.isBlank()) {
                String subject = "Nouvelle commande - Wouhouch Hub";
                String html = EmailTemplates.adminNewOrder(
                        saved.getId(),
                        saved.getCustomerName(),
                        saved.getEmail(),
                        saved.getPhone(),
                        saved.getTotal() == null ? "0" : saved.getTotal().toString(),
                        saved.getCreatedAt() == null ? "" : saved.getCreatedAt().toString(),
                        items
                );
                log.info("Sending admin notification to {} (order={})", adminEmail, saved.getId());
                emailService.sendHtmlMessage(adminEmail, subject, html);
            } else {
                log.warn("Admin email not configured; skipping admin notification for order {}", saved.getId());
            }
        } catch (Exception ex) {
            log.error("Failed sending admin notification for order {}: {}", saved.getId(), ex.getMessage(), ex);
        }

        return saved;
    }

    @Override
    public Order update(String id, Order order) {
        Optional<Order> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) throw new IllegalArgumentException("Order not found");

        // Default status should be PENDING if not provided
        if (order.getStatus() == null) order.setStatus(OrderStatus.PENDING);

        // preserve id
        order.setId(id);
        Order saved = repository.save(order);

        // Do NOT send emails on update — mails are only sent on creation
        log.info("Order updated (no email sent) id={}", saved.getId());

        return saved;
    }

    @Override
    public Optional<Order> getById(String id) {
        return repository.findById(id);
    }

    @Override
    public List<Order> getAll() {
        return repository.findAll();
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        // delete order items first to avoid possible FK constraints
        try {
            log.info("Deleting all order items and orders");
            if (itemRepo != null) {
                try {
                    itemRepo.deleteAll();
                } catch (Exception ex) {
                    log.warn("Failed to delete order items via deleteAll(), attempting delete by order ids: {}", ex.getMessage());
                }
            }
            repository.deleteAll();
        } catch (Exception ex) {
            log.error("Failed to delete all orders: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

}
