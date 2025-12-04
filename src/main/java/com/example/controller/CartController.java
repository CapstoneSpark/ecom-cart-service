

package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.model.Cart;
import com.example.controller.CartItemRequest;
import com.example.service.CartService;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) { 
        this.cartService = cartService; 
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Cart> getCart(@PathVariable String userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/{userId}/item")
    public ResponseEntity<Cart> addItem(
            @PathVariable String userId,
            @RequestBody CartItemRequest request) {

        Cart updatedCart = cartService.addItemToCart(userId, request.getSku(), request.getQuantity());
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/{userId}/item/{sku}")
    public ResponseEntity<Cart> removeItem(@PathVariable String userId, @PathVariable String sku) {
        Cart updatedCart = cartService.removeItemFromCart(userId, sku);
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
