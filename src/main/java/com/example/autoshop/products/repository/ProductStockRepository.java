package com.example.autoshop.products.repository;

import com.example.autoshop.products.model.Product;
import com.example.autoshop.products.model.ProductStock;
import com.example.autoshop.supply.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductStockRepository extends JpaRepository<ProductStock, Long>, QuerydslPredicateExecutor<ProductStock> {
    Optional<ProductStock> findByProductAndWarehouse(
            Product product,
            Warehouse warehouse
    );
}
