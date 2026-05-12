package com.example.autoshop.users.repository;

import com.example.autoshop.users.model.PriceLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceLevelRepository extends JpaRepository<PriceLevel, Long>, QuerydslPredicateExecutor<PriceLevel> {
}
