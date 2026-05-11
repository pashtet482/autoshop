package com.example.autoshop.products.repository;

import com.example.autoshop.supply.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long>, QuerydslPredicateExecutor<Warehouse> {
}
