package com.example.autoshop.products.repository;

import com.example.autoshop.products.model.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long>, QuerydslPredicateExecutor<ProductAttribute> {
}
