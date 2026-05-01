package com.example.autoshop.orders.model;

import com.example.autoshop.users.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "orders")
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "status", nullable = false)
    private OrderStatus orderStatus;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "date_of_purchase", nullable = false)
    private OffsetDateTime dateOfPurchase = OffsetDateTime.now();

    @Column(name = "date_of_delivery", nullable = false)
    private OffsetDateTime dateOfDelivery;

    @Column(name = "delivery_address", nullable = false)
    private String deliveryAddress;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;
}