package com.example.autoshop.supply.repository;

import com.example.autoshop.supply.model.SupplyItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplyItemsRepository extends JpaRepository<SupplyItem, Long>, QuerydslPredicateExecutor<SupplyItem> {
}
