package com.example.autoshop.products.repository;

import com.example.autoshop.orders.model.ProductInOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductInOrderRepository extends JpaRepository<ProductInOrder, Long>, QuerydslPredicateExecutor<ProductInOrder> {
}
