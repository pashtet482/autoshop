package com.example.autoshop.products.repository;

import com.example.autoshop.products.dto.ProductDTO;
import com.example.autoshop.products.model.Product;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<ProductDTO> findAllBy();
    @NonNull Optional<ProductDTO> findProjectedById(@NonNull Long id);
}
