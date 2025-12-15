package com.example.demo.service.impl;

import com.example.demo.DTO.CreateOrderRequest;
import com.example.demo.DTO.OrderLineRequest;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.service.ShopService;
import com.example.demo.service.email.EmailService;
import com.example.demo.service.email.EmailTemplates;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ProductRepository productRepo;
    private final ProductVariantRepository variantRepo;
    private final OrderRepository orderRepo;
    private final OrderItemRepository itemRepo;
    private final PaymentRepository paymentRepo;
    private final MongoTemplate mongoTemplate;
    private final EmailService emailService;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Override
    @Transactional
    public Order createOrder(CreateOrderRequest req) {
        // 1) Validate items exist + compute total from DB prices (never trust frontend)
        BigDecimal total = BigDecimal.ZERO;

        Order order = Order.builder()
                .customerName(req.getCustomerName())
                .email(req.getEmail().toLowerCase())
                .phone(req.getPhone())
                .address(Address.builder()
                        .country(req.getAddress().getCountry())
                        .city(req.getAddress().getCity())
                        .postalCode(req.getAddress().getZip())
                        .street(req.getAddress().getLine1())
                        .state(req.getAddress().getLine2())
                        .build())
                .status(OrderStatus.PENDING) // Default to Pending after creation
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Order savedOrder = orderRepo.save(order);

        for (OrderLineRequest line : req.getItems()) {
            Product p = productRepo.findById(line.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            if (!Boolean.TRUE.equals(p.getIsActive())) throw new IllegalArgumentException("Product inactive");

            int qty = line.getQty();

            String variantSnapshot = "";
            String variantId = line.getVariantId();

            // 2) decrement stock (variant or product) atomically
            if (variantId != null && !variantId.isBlank()) {
                var r = mongoTemplate.updateFirst(
                        Query.query(Criteria.where("_id").is(variantId).and("stock").gte(qty)),
                        new Update().inc("stock", -qty).set("updatedAt", Instant.now()),
                        ProductVariant.class
                );
                if (r.getModifiedCount() != 1) throw new IllegalStateException("OUT_OF_STOCK");

                ProductVariant v = variantRepo.findById(variantId).orElseThrow();
                variantSnapshot = v.getVariantType() + ":" + v.getValue();
            } else {
                if (Boolean.TRUE.equals(p.getHasVariants())) {
                    throw new IllegalArgumentException("Variant required for this product");
                }
                var r = mongoTemplate.updateFirst(
                        Query.query(Criteria.where("_id").is(p.getId()).and("stock").gte(qty)),
                        new Update().inc("stock", -qty).set("updatedAt", Instant.now()),
                        Product.class
                );
                if (r.getModifiedCount() != 1) throw new IllegalStateException("OUT_OF_STOCK");
            }

            // 3) create item snapshot
            BigDecimal unitPrice = p.getPrice();
            total = total.add(unitPrice.multiply(BigDecimal.valueOf(qty)));

            itemRepo.save(OrderItem.builder()
                    .orderId(savedOrder.getId())
                    .productId(p.getId())
                    .productTitleSnapshot(p.getTitle())
                    .variantId((variantId == null || variantId.isBlank()) ? null : variantId)
                    .variantSnapshot(variantSnapshot.isBlank() ? null : variantSnapshot)
                    .qty(qty)
                    .unitPrice(unitPrice)
                    .build());
        }

        // 4) update total
        savedOrder.setTotal(total);
        savedOrder.setUpdatedAt(Instant.now());
        savedOrder = orderRepo.save(savedOrder);

        // 5) payment record
        paymentRepo.save(Payment.builder()
                .orderId(savedOrder.getId())
                .provider(req.getProvider())
                .status(PaymentStatus.SUCCESS)
                .createdAt(Instant.now())
                .build());

        // 6) email confirmation to customer (do not rollback order if email fails)
        try {
            emailService.sendHtml(
                    savedOrder.getEmail(),
                    "Confirmation de commande - Wouhouch Hub",
                    EmailTemplates.orderConfirmation(savedOrder, itemRepo.findByOrderId(savedOrder.getId()))
            );
        } catch (Exception ignored) {
        }

        // 7) email notification to admin (do not rollback order if email fails)
        try {
            if (adminEmail != null && !adminEmail.isBlank()) {
                emailService.sendHtml(
                        adminEmail,
                        "Nouvelle commande - Wouhouch Hub",
                        EmailTemplates.adminOrderNotification(savedOrder, itemRepo.findByOrderId(savedOrder.getId()))
                );
            }
        } catch (Exception ignored) {
        }

        return savedOrder;
    }
}
