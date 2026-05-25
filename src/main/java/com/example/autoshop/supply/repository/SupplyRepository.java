package com.example.autoshop.supply.repository;

import com.example.autoshop.supply.model.Supply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplyRepository extends JpaRepository<Supply, Long>, QuerydslPredicateExecutor<Supply> {
    Page<Supply> findAllByIsDeletedFalse(Pageable pageable);
}
