package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.model.Cart;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    // Custom method to easily retrieve the cart using the user's ID string
    Optional<Cart> findByUserId(String userId);
}
