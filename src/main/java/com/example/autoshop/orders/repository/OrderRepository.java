package com.example.autoshop.orders.repository;

import com.example.autoshop.orders.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, QuerydslPredicateExecutor<Order> {

    Page<Order> findAllByUser_UsernameNot(String username, Pageable pageable);

    Page<Order> findAllByUser_UsernameAndIsDeletedFalse(String username, Pageable pageable);

    Page<Order> findAllByUser_UsernameNotAndIsDeletedFalse(String username, Pageable pageable);
}
