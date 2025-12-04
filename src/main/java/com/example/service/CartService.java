



package com.example.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
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
    private final WebClient webClient;

    @Autowired
    public CartService(CartRepository cartRepository, WebClient webClient) {
        this.cartRepository = cartRepository;
        this.webClient = webClient;
    }

    // ---------------- GET CART ----------------
    public Cart getCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for userId: " + userId));

        cart.calculateTotal();
        return cart;
    }

    // ---------------- FETCH PRODUCT ----------------
    @CircuitBreaker(name = "productCatalogService", fallbackMethod = "getProductFallback")
    public Product fetchProduct(String sku) {
        Product product = webClient.get()
                .uri("/api/v1/products/sku/{sku}", sku)
                .retrieve()
                .bodyToMono(Product.class)
                .block();

        if (product == null) {
            throw new RuntimeException("Product not found for SKU: " + sku);
        }

        return product;
    }

    public Product getProductFallback(String sku, Throwable ex) {
        throw new RuntimeException("Product service unavailable. Cannot fetch SKU: " + sku);
    }

    // ---------------- ADD ITEM ----------------
    @Transactional
    public Cart addItemToCart(String userId, String sku, int quantity) {

        // Create/find cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                });

        // Fetch latest product data
        Product product = fetchProduct(sku);

        // Stock validation
        if (product.getStock() == null || product.getStock() <= 0) {
            throw new RuntimeException("Product is out of stock");
        }
        if (quantity > product.getStock()) {
            throw new RuntimeException("Requested quantity exceeds stock");
        }

        // Check if item already exists
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getSku().equals(sku))
                .findFirst();

        if (existingItem.isPresent()) {
            // Already in cart → update quantity and sync product info
            CartItem item = existingItem.get();
            int newQty = item.getQuantity() + quantity;

            if (newQty > product.getStock()) {
                throw new RuntimeException("Not enough stock to increase quantity");
            }

            item.setQuantity(newQty);
            item.setPrice(product.getPrice());
            item.setName(product.getName());
            item.setBrand(product.getBrand());
            item.setImageUrl(product.getImageUrl());
            item.setProductId(product.getProductId());

        } else {
            // New cart item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setSku(product.getSku());
            newItem.setName(product.getName());
            newItem.setPrice(product.getPrice());
            newItem.setQuantity(quantity);

            // ⭐ IMPORTANT — NEW FIELDS
            newItem.setBrand(product.getBrand());
            newItem.setImageUrl(product.getImageUrl());
            newItem.setProductId(product.getProductId());

            cart.getItems().add(newItem);
        }

        cart.calculateTotal();
        return cartRepository.save(cart);
    }

    // ---------------- REMOVE ITEM ----------------
    @Transactional
    public Cart removeItemFromCart(String userId, String sku) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for userId: " + userId));

        cart.getItems().removeIf(item -> item.getSku().equals(sku));

        cart.calculateTotal();
        return cartRepository.save(cart);
    }

    // ---------------- CLEAR CART ----------------
    @Transactional
    public void clearCart(String userId) {
        Optional<Cart> opt = cartRepository.findByUserId(userId);
        if (opt.isEmpty()) return;

        Cart cart = opt.get();
        cart.getItems().clear();

        cartRepository.save(cart);
    }
}
