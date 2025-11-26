package com.example.demo.controller;

import com.example.demo.entity.Product;
import com.example.demo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

public class ProductControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setup() {
        productRepository.deleteAll();
    }

    @Test
    void createAndGetProduct() {
        String url = "/api/v1/products";

        Product p = new Product();
        p.setName("IT Product");
        p.setPrice(100.0);

        ResponseEntity<Product> post = restTemplate.postForEntity(url, p, Product.class);
        assertEquals(HttpStatus.OK, post.getStatusCode());
        assertNotNull(post.getBody());
        assertNotNull(post.getBody().getProductId());

        ResponseEntity<Product> get = restTemplate.getForEntity(url + "/" + post.getBody().getProductId(), Product.class);
        assertEquals(HttpStatus.OK, get.getStatusCode());
        assertEquals("IT Product", get.getBody().getName());
    }
}

