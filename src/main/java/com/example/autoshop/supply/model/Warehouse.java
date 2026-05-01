package com.example.autoshop.supply.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "warehouses")
@NoArgsConstructor
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "address", nullable = false)
    private String address;

    @NotBlank
    @Size(max = 15)
    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "delivery_days", nullable = false)
    private Integer deliveryDays;
}