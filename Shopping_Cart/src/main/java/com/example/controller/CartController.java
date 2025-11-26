package com.example.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.model.Cart;
import com.example.service.CartService;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    
    @Autowired
    public CartController(CartService cartService) {
		super();
		this.cartService = cartService;
	}
    

    // 1. GET /api/cart/{userId} - View Cart
    @GetMapping("/{userId}")
    public ResponseEntity<Cart> getCart(@PathVariable String userId) {
        // The service layer handles cart creation/total calculation if needed
        return ResponseEntity.ok(cartService.getCart(userId));
    }


	// 2. POST /api/cart/{userId}/item - Add/Update Item
    @PostMapping("/{userId}/item")
    public ResponseEntity<Cart> addItem(@PathVariable String userId, @RequestBody CartItemRequest request) {
        Cart updatedCart = cartService.addItemToCart(userId, request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(updatedCart);
    }

    // 3. DELETE /api/cart/{userId}/item/{productId} - Remove Item
    @DeleteMapping("/{userId}/item/{productId}")
    public ResponseEntity<Cart> removeItem(@PathVariable String userId, @PathVariable String productId) {
        Cart updatedCart = cartService.removeItemFromCart(userId, productId);
        return ResponseEntity.ok(updatedCart);
    }

    // 4. DELETE /api/cart/{userId} - Clear Cart
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build(); // Standard response for successful deletion with no body
    }
}
