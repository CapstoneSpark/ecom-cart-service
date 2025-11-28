//package com.example.service;
//
//import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
//import jakarta.transaction.Transactional;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import com.example.model.Cart;
//import com.example.model.CartItem;
//import com.example.model.Product;
//import com.example.repository.CartRepository;
//
//import java.util.Optional;
//
//@Service
//public class CartService {
//
//    private final CartRepository cartRepository;
//    private final WebClient webClient;
//
//    @Autowired
//    public CartService(CartRepository cartRepository, WebClient webClient) {
//        this.cartRepository = cartRepository;
//        this.webClient = webClient;
//    }
//
//    // ---------------- GET CART ----------------
//    public Cart getCart(String userId) {
//        Cart cart = cartRepository.findByUserId(userId)
//                .orElseThrow(() -> new RuntimeException("Cart not found for userId: " + userId));
//        cart.calculateTotal();
//        return cart;
//    }
//
//    // ---------------- FETCH PRODUCT ----------------
//    @CircuitBreaker(name = "productCatalogService", fallbackMethod = "getProductFallback")
//    public Product fetchProduct(String sku) {
//
//        Product product = webClient.get()
//                .uri("/api/v1/products/sku/{sku}", sku)
//                .retrieve()
//                .bodyToMono(Product.class)
//                .block();
//
//        if (product == null) {
//            throw new RuntimeException("Product not found for SKU: " + sku);
//        }
//
//        return product;
//    }
//
//    public Product getProductFallback(String sku, Throwable ex) {
//        throw new RuntimeException("Product service unavailable. Cannot fetch SKU: " + sku);
//    }
//
//    // ---------------- ADD ITEM ----------------
//    @Transactional
//    public Cart addItemToCart(String userId, String sku, int quantity) {
//
//        // Create or get cart
//        Cart cart = cartRepository.findByUserId(userId)
//                .orElseGet(() -> {
//                    Cart c = new Cart();
//                    c.setUserId(userId);
//                    return cartRepository.save(c);
//                });
//
//        // Fetch latest product details
//        Product product = fetchProduct(sku);
//
//        // 1️⃣ STOCK VALIDATION
//        if (product.getStock() == null || product.getStock() <= 0) {
//            throw new RuntimeException("Product is out of stock");
//        }
//        if (quantity > product.getStock()) {
//            throw new RuntimeException("Requested quantity exceeds available stock");
//        }
//
//       
//
//        // Check if item already in cart
//        Optional<CartItem> existingItem = cart.getItems().stream()
//                .filter(item -> item.getProductId().equals(sku))
//                .findFirst();
//
//        if (existingItem.isPresent()) {
//            CartItem item = existingItem.get();
//
//            int newQuantity = item.getQuantity() + quantity;
//
//            // again check stock when updating
//            if (newQuantity > product.getStock()) {
//                throw new RuntimeException("Not enough stock to increase quantity");
//            }
//
//            // 3️⃣ PRICE CONSISTENCY
//            item.setPrice(product.getPrice());
//            item.setName(product.getName());
//
//            item.setQuantity(newQuantity);
//
//        } else {
//
//            CartItem newItem = new CartItem();
//            newItem.setCart(cart);
//            newItem.setProductId(product.getSku());
//            newItem.setName(product.getName());
//            newItem.setPrice(product.getPrice());
//            newItem.setQuantity(quantity);
//
//            cart.getItems().add(newItem);
//        }
//
//        cart.calculateTotal();
//        return cartRepository.save(cart);
//    }
//
//    // ---------------- REMOVE ITEM ----------------
//    @Transactional
//    public Cart removeItemFromCart(String userId, String productSku) {
//        Cart cart = cartRepository.findByUserId(userId)
//                .orElseThrow(() -> new RuntimeException("Cart not found for userId: " + userId));
//
//        cart.getItems().removeIf(item -> item.getProductId().equals(productSku));
//
//        cart.calculateTotal();
//        return cartRepository.save(cart);
//    }
//
//    // ---------------- CLEAR CART ----------------
//    @Transactional
//    public void clearCart(String userId) {
//        Cart cart = cartRepository.findByUserId(userId)
//                .orElseThrow(() -> new RuntimeException("Cart not found for userId: " + userId));
//
//        cart.getItems().clear();
//        cartRepository.save(cart);
//    }
//}



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

    public Cart getCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for userId: " + userId));
        cart.calculateTotal();
        return cart;
    }

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

    @Transactional
    public Cart addItemToCart(String userId, String sku, int quantity) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setUserId(userId);
                    return cartRepository.save(c);
                });

        Product product = fetchProduct(sku);

        if (product.getStock() == null || product.getStock() <= 0) {
            throw new RuntimeException("Product is out of stock");
        }
        if (quantity > product.getStock()) {
            throw new RuntimeException("Requested quantity exceeds available stock");
        }

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getSku().equals(sku))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;

            if (newQuantity > product.getStock()) {
                throw new RuntimeException("Not enough stock to increase quantity");
            }

            item.setPrice(product.getPrice());
            item.setName(product.getName());
            item.setQuantity(newQuantity);

        } else {

            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setSku(product.getSku());
            newItem.setName(product.getName());
            newItem.setPrice(product.getPrice());
            newItem.setQuantity(quantity);

            cart.getItems().add(newItem);
        }

        cart.calculateTotal();
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeItemFromCart(String userId, String sku) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for userId: " + userId));

        cart.getItems().removeIf(item -> item.getSku().equals(sku));

        cart.calculateTotal();
        return cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(String userId) {
        Optional<Cart> opt = cartRepository.findByUserId(userId);
        if (opt.isEmpty()) {
            // nothing to clear - don't treat as error
            return;
        }
        Cart cart = opt.get();
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    
    

}
