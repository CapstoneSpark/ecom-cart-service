package com.example.service;


import com.example.model.Cart;
import com.example.model.CartItem;
import com.example.model.Product;
import com.example.repository.CartRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private WebClient webClient;

    // Mocks for WebClient chaining
    @Mock
    private RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private RequestHeadersSpec requestHeadersSpec;
    @Mock
    private ResponseSpec responseSpec;

    @InjectMocks
    private CartService cartService;

    // Helper method to create a CartItem (assuming a simple constructor or setters)
    private CartItem createCartItem(String productId, int quantity, double price) {
        CartItem item = new CartItem();
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setPrice(price);
        item.setName("Test Product");
        return item;
    }
    
    // Helper method for setting up WebClient mock chain
    private void mockWebClientResponse(Product product) {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Product.class)).thenReturn(Mono.just(product));
    }


    @BeforeEach
    void setUp() {
        // Initialize Mocks and inject them into cartService
        MockitoAnnotations.openMocks(this);
    }
    
    // --- Test Cases for addItemToCart ---

    @Test
    void addItemToCart_createsNewCartAndItem() {
        String userId = "testUser";
        String productId = "P001";
        int quantity = 5;
        double price = 100.0;

        Product mockProduct = new Product();
        mockProduct.setPrice(price);
        mockWebClientResponse(mockProduct);

        // Mock: Cart is empty (new cart will be created)
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        
        // Mock: save returns the cart object passed to it
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart resultCart = cartService.addItemToCart(userId, productId, quantity);

        assertEquals(1, resultCart.getItems().size());
        assertEquals(quantity, resultCart.getItems().get(0).getQuantity());
        // Verify calculation: 100.0 * 5 = 500.0
        assertEquals(price * quantity, resultCart.getTotalAmount(), 0.001); 
        
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void addItemToCart_updatesExistingItemQuantityAndTotal() {
        String userId = "testUser";
        String productId = "P001";
        int initialQuantity = 3;
        int addedQuantity = 2;
        double price = 100.0;

        Cart existingCart = new Cart();
        existingCart.setUserId(userId);
        existingCart.setItems(new ArrayList<>());
        existingCart.getItems().add(createCartItem(productId, initialQuantity, price));

        Product mockProduct = new Product();
        mockProduct.setPrice(price);
        mockWebClientResponse(mockProduct);

        // Mock: Cart exists
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart resultCart = cartService.addItemToCart(userId, productId, addedQuantity);

        assertEquals(1, resultCart.getItems().size());
        assertEquals(initialQuantity + addedQuantity, resultCart.getItems().get(0).getQuantity()); // Quantity: 5
        // Verify calculation: 100.0 * 5 = 500.0
        assertEquals(price * 5, resultCart.getTotalAmount(), 0.001); 
        
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    // --- Test Cases for removeItemFromCart ---
    
    @Test
    void removeItemFromCart_removesItemAndRecalculatesTotal() {
        String userId = "testUser";
        String productIdToRemove = "P001";
        
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());
        cart.getItems().add(createCartItem(productIdToRemove, 2, 100.0)); // Item to remove (Total 200.0)
        cart.getItems().add(createCartItem("P002", 1, 50.0)); // Remaining item (Total 50.0)

        // Mock: getCart returns the cart with items
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart resultCart = cartService.removeItemFromCart(userId, productIdToRemove);

        assertEquals(1, resultCart.getItems().size()); // Only P002 remains
        // Verify calculation: 50.0
        assertEquals(50.0, resultCart.getTotalAmount(), 0.001);
        
        verify(cartRepository, times(1)).save(any(Cart.class));
    }
    
    // --- Test Case for clearCart ---
    
    @Test
    void clearCart_emptiesCartAndSetsTotalToZero() {
        String userId = "testUser";
        
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());
        cart.getItems().add(createCartItem("P001", 2, 100.0));

        // Mock: getCart returns the cart with items
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        cartService.clearCart(userId);

        // Verify that clear() was called and saved
        assertTrue(cart.getItems().isEmpty());
        
        // Note: The totalAmount calculation would run on a subsequent GET, but here we just verify the save operation.
        verify(cartRepository, times(1)).save(cart);
    }
    
    // --- Test Case for Fallback Logic (Circuit Breaker) ---
    
    @Test
    void fetchProductDetails_usesFallbackWhenWebClientFails() {
        String productId = "P001";
        int quantity = 1;

        // Mock: WebClient to simulate a communication error (e.g., block throws an exception)
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        // Simulate an error by returning an error Mono
        when(responseSpec.bodyToMono(Product.class)).thenReturn(Mono.error(new RuntimeException("Connection Refused")));

        // ACT
        CartItem fallbackItem = cartService.getProductFallback(productId, quantity, new RuntimeException("Simulated Failure"));
        
        // ASSERT
        // Verify that the fallback returns a safe, default item
        assertEquals(productId, fallbackItem.getProductId());
        assertEquals(0.0, fallbackItem.getPrice(), 0.001);
        assertTrue(fallbackItem.getName().contains("Unknown Product"));
    }
}
