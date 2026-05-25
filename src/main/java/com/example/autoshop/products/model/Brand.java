package com.example.autoshop.products.model;

import com.example.autoshop.common.model.SoftDeletable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "brands")
@NoArgsConstructor
public class Brand extends SoftDeletable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    @Size(max = 128)
    @NotBlank
    private String name;

    @NotBlank
    @Size(max = 64)
    @Column(name = "country", nullable = false)
    private String country;
}
