package com.example.demo.service;


import com.example.demo.entity.Product;
import com.example.demo.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
public class ProductServiceImplTest {

	  private final ProductRepository productRepository = Mockito.mock(ProductRepository.class);
	    private final ProductServiceImpl productService = new ProductServiceImpl(productRepository);

	    @Test
	    void createProduct_shouldSaveAndReturn() {
	        Product p = new Product();
	        p.setName("Test");
	        p.setPrice(10.0);

	        when(productRepository.save(any(Product.class))).thenReturn(p);

	        Product saved = productService.createProduct(p);
	        assertEquals("Test", saved.getName());
	        verify(productRepository, times(1)).save(p);
	    }

	    @Test
	    void getProductById_notFound_shouldThrow() {
	        when(productRepository.findById(1L)).thenReturn(Optional.empty());
	        RuntimeException ex = assertThrows(RuntimeException.class, () -> productService.getProductById(1L));
	        assertTrue(ex.getMessage().contains("Product not found"));
	    }
	}
	

