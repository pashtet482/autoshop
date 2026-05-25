package com.example.autoshop.users.model;

import com.example.autoshop.common.model.SoftDeletable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "price_levels")
public class PriceLevel extends SoftDeletable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "ratio", precision = 5, scale = 2, nullable = false)
    private BigDecimal ratio;
}
