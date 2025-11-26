package com.example.demo.controller;

import com.example.demo.entity.Product;
import com.example.demo.entity.ProductCategory;
import com.example.demo.repository.ProductCategoryRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository;

    public ProductController(ProductService productService,
                             ProductCategoryRepository productCategoryRepository,
                             ProductRepository productRepository) {
        this.productService = productService;
        this.productCategoryRepository = productCategoryRepository;
        this.productRepository = productRepository;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product saved = productService.createProduct(product);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(Pageable pageable) {
        Page<Product> page = productRepository.findAll(pageable);
        return ResponseEntity.ok(page);
    }

    // Existing: Looks up by internal Long ID
    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    // ✨ NEW ENDPOINT: Looks up by String SKU/Product ID used by the Cart Service
    @GetMapping("/sku/{sku}")
    public ResponseEntity<Product> getProductBySku(@PathVariable String sku) {
        // We will implement getProductBySku in ProductService
        Product product = productService.getProductBySku(sku); 
        if (product == null) {
            // Returning 404 Not Found if the product isn't found by SKU
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long productId,
                                                 @RequestBody Product product) {
        return ResponseEntity.ok(productService.updateProduct(productId, product));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam("q") String keyword) {
        return ResponseEntity.ok(productRepository.searchByKeyword(keyword));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable Long categoryId) {
        List<ProductCategory> mappings = productCategoryRepository.findByCategoryId(categoryId);
        List<Product> products = mappings.stream()
                .map(ProductCategory::getProduct)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }
}