package com.example.demo.controller;

import com.example.demo.model.Order;
import com.example.demo.model.OrderItem;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderExportController {

    private final OrderService orderService;
    private final OrderItemRepository itemRepo;

    @GetMapping(path = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportOrdersCsv() {
        List<Order> orders = orderService.getAll();

        StringBuilder sb = new StringBuilder();
        // Header
        sb.append("OrderId,CustomerName,Email,Phone,Status,Total,CreatedAt,Items\n");

        for (Order o : orders) {
            List<OrderItem> items = itemRepo.findByOrderId(o.getId());
            String itemsStr = "";
            if (items != null && !items.isEmpty()) {
                itemsStr = items.stream()
                        .map(it -> escapeCsv(it.getProductTitleSnapshot()) + " x" + (it.getQty() == null ? 0 : it.getQty()))
                        .collect(Collectors.joining("; "));
            }

            sb.append(csvField(o.getId()))
                    .append(',').append(csvField(o.getCustomerName()))
                    .append(',').append(csvField(o.getEmail()))
                    .append(',').append(csvField(o.getPhone()))
                    .append(',').append(csvField(o.getStatus() == null ? "" : o.getStatus().name()))
                    .append(',').append(csvField(o.getTotal() == null ? "" : o.getTotal().toString()))
                    .append(',').append(csvField(o.getCreatedAt() == null ? "" : o.getCreatedAt().toString()))
                    .append(',').append(csvField(itemsStr))
                    .append('\n');
        }

        byte[] csvBytes = sb.toString().getBytes(StandardCharsets.UTF_8);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "orders_export_" + timestamp + ".csv";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        headers.setContentLength(csvBytes.length);

        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
    }

    private static String csvField(String s) {
        if (s == null) return "";
        return escapeCsv(s);
    }

    // Escape CSV by wrapping in double quotes and doubling internal quotes
    private static String escapeCsv(String s) {
        if (s == null) return "";
        String safe = s.replace("\"", "\"\"");
        if (safe.contains(",") || safe.contains("\n") || safe.contains("\r") || safe.contains("\"")) {
            return "\"" + safe + "\"";
        }
        return safe;
    }
}

