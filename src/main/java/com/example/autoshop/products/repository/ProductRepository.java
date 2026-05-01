package com.example.autoshop.products.repository;

import com.example.autoshop.products.dto.GetProductsList;
import com.example.autoshop.products.model.Product;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<GetProductsList> findBy();
    @NonNull Optional<GetProductsList> findProjectedById(@NonNull Long id);
}
