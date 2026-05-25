package com.example.autoshop.orders.controller;

import com.example.autoshop.orders.dto.InputOrderDTO;
import com.example.autoshop.orders.dto.OrderDTO;
import com.example.autoshop.orders.model.OrderStatus;
import com.example.autoshop.orders.service.OrderService;
import com.example.autoshop.orders.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ReceiptService receiptService;

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(
            Authentication authentication,
            @RequestBody InputOrderDTO dto
    ) {
        boolean adminMode = isAdmin(authentication);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder(dto, authentication.getName(), adminMode));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrder(
            Authentication authentication,
            @PathVariable Long id
    ) {
        boolean adminMode = isAdmin(authentication);
        return ResponseEntity.ok(
                orderService.getOrderById(id, authentication.getName(), adminMode)
        );
    }

    @GetMapping
    public ResponseEntity<Page<OrderDTO>> getOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean otherUsers
    ) {
        boolean adminMode = isAdmin(authentication);
        return ResponseEntity.ok(
                orderService.getAllOrders(
                        page,
                        size,
                        authentication.getName(),
                        adminMode,
                        otherUsers
                )
        );
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status
    ) {
        return ResponseEntity.ok(
                orderService.updateStatus(id, status)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable Long id
    ) {
        orderService.deleteOrder(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/receipt")
    public ResponseEntity<byte[]> generateReceipt(
            Authentication authentication,
            @PathVariable Long id
    ) {
        boolean adminMode = isAdmin(authentication);

        byte[] pdf = receiptService.generateReceipt(id, authentication.getName(), adminMode);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=receipt.pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}

