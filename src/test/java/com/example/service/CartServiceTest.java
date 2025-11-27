//package com.example.service;
//
//import com.example.model.Cart;
//import com.example.model.CartItem;
//import com.example.model.Product;
//import com.example.repository.CartRepository;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
//import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
//import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
//
//import reactor.core.publisher.Mono;
//
//import java.util.ArrayList;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//public class CartServiceTest {
//
//    @Mock
//    private CartRepository cartRepository;
//
//    @Mock
//    private WebClient webClient;
//
//    @Mock
//    private RequestHeadersUriSpec requestHeadersUriSpec;
//
//    @Mock
//    private RequestHeadersSpec requestHeadersSpec;
//
//    @Mock
//    private ResponseSpec responseSpec;
//
//    @InjectMocks
//    private CartService cartService;
//
//    // -----------------------------------------
//    // Helper: Create CartItem
//    // -----------------------------------------
//    private CartItem createCartItem(Long productId, int quantity, double price) {
//        CartItem item = new CartItem();
//        item.setProductId(productId);
//        item.setQuantity(quantity);
//        item.setPrice(price);
//        item.setName("Test Product");
//        return item;
//    }
//
//    // -----------------------------------------
//    // Helper: Mock ProductService Response
//    // -----------------------------------------
//    private void mockWebClientResponse(Product product) {
//        when(webClient.get()).thenReturn(requestHeadersUriSpec);
//        when(requestHeadersUriSpec.uri("/api/v1/products/{id}", product.getId()))
//                .thenReturn(requestHeadersSpec);
//        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
//        when(responseSpec.bodyToMono(Product.class)).thenReturn(Mono.just(product));
//    }
//
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    // ============================================================
//    // Test: Add Item to Cart - New Cart
//    // ============================================================
//    @Test
//    void addItemToCart_createsNewCartAndItem() {
//
//        String userId = "testUser";
//        Long productId = 1L;
//        int quantity = 5;
//        double price = 100.0;
//
//        Product mockProduct = new Product();
//        mockProduct.setId(productId);
//        mockProduct.setPrice(price);
//        mockProduct.setName("Test Product");
//
//        mockWebClientResponse(mockProduct);
//
//        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
//        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        Cart resultCart = cartService.addItemToCart(userId, productId, quantity);
//
//        assertEquals(1, resultCart.getItems().size());
//        assertEquals(quantity, resultCart.getItems().get(0).getQuantity());
//        assertEquals(price * quantity, resultCart.getTotalAmount(), 0.001);
//
//        verify(cartRepository, times(1)).save(any(Cart.class));
//    }
//
//    // ============================================================
//    // Test: Add Item to Existing Cart
//    // ============================================================
//    @Test
//    void addItemToCart_updatesExistingItemQuantityAndTotal() {
//
//        String userId = "testUser";
//        Long productId = 1L;
//        int initialQuantity = 3;
//        int addedQuantity = 2;
//        double price = 100.0;
//
//        Cart existingCart = new Cart();
//        existingCart.setUserId(userId);
//        existingCart.setItems(new ArrayList<>());
//        existingCart.getItems().add(createCartItem(productId, initialQuantity, price));
//
//        Product mockProduct = new Product();
//        mockProduct.setId(productId);
//        mockProduct.setPrice(price);
//        mockProduct.setName("Test Product");
//
//        mockWebClientResponse(mockProduct);
//
//        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));
//        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        Cart resultCart = cartService.addItemToCart(userId, productId, addedQuantity);
//
//        assertEquals(1, resultCart.getItems().size());
//        assertEquals(initialQuantity + addedQuantity, resultCart.getItems().get(0).getQuantity());
//        assertEquals(price * (initialQuantity + addedQuantity), resultCart.getTotalAmount(), 0.001);
//
//        verify(cartRepository, times(1)).save(any(Cart.class));
//    }
//
//    // ============================================================
//    // Test: Remove Item from Cart
//    // ============================================================
//    @Test
//    void removeItemFromCart_removesItemAndRecalculatesTotal() {
//
//        String userId = "testUser";
//        Long removeProductId = 1L;
//
//        Cart cart = new Cart();
//        cart.setUserId(userId);
//        cart.setItems(new ArrayList<>());
//        cart.getItems().add(createCartItem(removeProductId, 2, 100.0)); // removed
//        cart.getItems().add(createCartItem(2L, 1, 50.0)); // remains
//
//        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
//        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        Cart resultCart = cartService.removeItemFromCart(userId, removeProductId);
//
//        assertEquals(1, resultCart.getItems().size());
//        assertEquals(50.0, resultCart.getTotalAmount(), 0.001);
//
//        verify(cartRepository, times(1)).save(any(Cart.class));
//    }
//
//    // ============================================================
//    // Test: Clear Cart
//    // ============================================================
//    @Test
//    void clearCart_emptiesCartAndSetsTotalToZero() {
//
//        String userId = "testUser";
//
//        Cart cart = new Cart();
//        cart.setUserId(userId);
//        cart.setItems(new ArrayList<>());
//        cart.getItems().add(createCartItem(1L, 2, 100.0));
//
//        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
//
//        cartService.clearCart(userId);
//
//        assertTrue(cart.getItems().isEmpty());
//
//        verify(cartRepository, times(1)).save(cart);
//    }
//
//    // ============================================================
//    // Test: Fallback Logic (CircuitBreaker)
//    // ============================================================
//    @Test
//    void fetchProductDetails_usesFallbackWhenWebClientFails() {
//
//        Long productId = 1L;
//        int quantity = 1;
//
//        when(webClient.get()).thenReturn(requestHeadersUriSpec);
//        when(requestHeadersUriSpec.uri(anyString(), anyLong())).thenReturn(requestHeadersSpec);
//        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
//
//        when(responseSpec.bodyToMono(Product.class))
//                .thenReturn(Mono.error(new RuntimeException("Connection Failed")));
//
//        CartItem fallbackItem = cartService.getProductFallback(productId, quantity, new RuntimeException("Fail"));
//
//        assertEquals(productId, fallbackItem.getProductId());
//        assertEquals(0.0, fallbackItem.getPrice(), 0.001);
//        assertTrue(fallbackItem.getName().contains("fallback"));
//    }
//}
