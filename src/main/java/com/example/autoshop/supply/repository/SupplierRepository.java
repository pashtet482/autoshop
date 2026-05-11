package com.example.autoshop.supply.repository;

import com.example.autoshop.supply.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface SupplierRepository extends JpaRepository<Supplier, Long>, QuerydslPredicateExecutor<Supplier> {
}
