package com.example.demo.service;

import com.example.demo.entity.Product;

import java.util.List;

public interface ProductService {

    Product createProduct(Product product);

    Product updateProduct(Long productId, Product product);

    Product getProductById(Long productId);

    List<Product> getAllProducts();
    
    Product getProductBySku(String sku);

    void deleteProduct(Long productId);
}
