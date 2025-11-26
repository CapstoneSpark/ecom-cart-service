package com.example.service;


import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.model.Cart;
import com.example.model.CartItem;
import com.example.model.Product;
import com.example.repository.CartRepository;

import java.util.Optional;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final WebClient webClient; // Configured in WebClientConfig
    
    @Autowired
    public CartService(CartRepository cartRepository, WebClient webClient) {
        this.cartRepository = cartRepository;
        this.webClient = webClient;
    }

    // --- Helper Method to Get Cart ---
    public Cart getCart(String userId) {
        // We calculate the total amount here before returning to the controller
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(new Cart());
        cart.calculateTotal();
        return cart;
    }

    @CircuitBreaker(name = "productCatalogService", fallbackMethod = "getProductFallback")
    public CartItem fetchProductDetails(String productId, int quantity) {
        // Synchronous call to external service
        Product product = webClient.get()
            // ✨ CHANGE HERE: Use the new /sku/{sku} path
            .uri("/api/products/sku/{id}", productId) 
            .retrieve()
            .bodyToMono(Product.class)
            .block(); 
        
        // ... rest of the method is unchanged
        if (product != null) {
            CartItem item = new CartItem();
            item.setProductId(productId);
            item.setName(product.getName());
            item.setPrice(product.getPrice());
            item.setQuantity(quantity);
            return item;
        }
        throw new RuntimeException("Product not found"); 
    }

    public CartItem getProductFallback(String productId, int quantity, Throwable t) {
        System.err.println("--- Product Catalog Service Failure. Returning fallback item. Error: " + t.getMessage());
        // Return a safe item to prevent transaction failure, but warn the user on the frontend.
        CartItem item = new CartItem();
        item.setProductId(productId);
        item.setName("Unknown Product (Check back later)");
        item.setPrice(0.0);
        item.setQuantity(quantity);
        return item;
    }
    
    // --- Core Logic: Add/Update Item ---
    @Transactional
    public Cart addItemToCart(String userId, String productId, int quantity) {
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUserId(userId);
           // newCart.getItems().size();
            //newCart.calculateTotal();
            return newCart;
        });

        CartItem newItemDetails = fetchProductDetails(productId, quantity);

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setPrice(newItemDetails.getPrice()); 
            item.setName(newItemDetails.getName());
        } else {
            newItemDetails.setCart(cart); 
            cart.getItems().add(newItemDetails);
        }
        cart.calculateTotal();
        return cartRepository.save(cart);
    }
    
    // --- Core Logic: Remove Item ---
    @Transactional
    public Cart removeItemFromCart(String userId, String productId) {
        Cart cart = getCart(userId); // Fetches and calculates total

        cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .ifPresentOrElse(
                    item -> cart.getItems().remove(item), // Deletes item due to orphanRemoval=true
                    () -> { throw new RuntimeException("Product ID " + productId + " not found in the cart."); }
                );
        cart.calculateTotal();

        return cartRepository.save(cart);
    }

    // --- Core Logic: Clear Cart ---
    @Transactional
    public void clearCart(String userId) {
        Cart cart = getCart(userId);
        // Clearing the collection (and saving) removes all child items due to orphanRemoval=true
        cart.getItems().clear();
        cartRepository.save(cart);
        // Alternative: cartRepository.delete(cart); // Deletes the cart record itself.
        // We save the empty cart to preserve the user's cart ID until they checkout or it expires.
    }
}