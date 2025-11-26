package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.model.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // Custom methods could be added here later (e.g., find all items for a specific product ID)
}
