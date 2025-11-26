package com.example.demo.controller;

import com.example.demo.entity.Category;
import com.example.demo.entity.Product;
import com.example.demo.entity.ProductCategory;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductCategoryRepository;
import com.example.demo.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product-categories")
public class ProductCategoryController {

    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductCategoryController(ProductCategoryRepository productCategoryRepository,
                                     ProductRepository productRepository,
                                     CategoryRepository categoryRepository) {
        this.productCategoryRepository = productCategoryRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @PostMapping
    public ResponseEntity<ProductCategory> createMapping(@RequestBody ProductCategoryRequest request) {

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + request.getProductId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found: " + request.getCategoryId()));

        ProductCategory mapping = new ProductCategory(product, category);
        ProductCategory saved = productCategoryRepository.save(mapping);

        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<ProductCategory>> getAllMappings() {
        return ResponseEntity.ok(productCategoryRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductCategory> getMappingById(@PathVariable Long id) {
        ProductCategory mapping = productCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mapping not found: " + id));
        return ResponseEntity.ok(mapping);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductCategory> updateMapping(@PathVariable Long id,
                                                         @RequestBody ProductCategoryRequest request) {

        ProductCategory existing = productCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mapping not found: " + id));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + request.getProductId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found: " + request.getCategoryId()));

        existing.setProduct(product);
        existing.setCategory(category);

        ProductCategory updated = productCategoryRepository.save(existing);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMapping(@PathVariable Long id) {
        productCategoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // get by product
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductCategory>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(productCategoryRepository.findByProductId(productId));
    }

    // get by category
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductCategory>> getByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(productCategoryRepository.findByCategoryId(categoryId));
    }

    public static class ProductCategoryRequest {
        private Long productId;
        private Long categoryId;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    }
}
