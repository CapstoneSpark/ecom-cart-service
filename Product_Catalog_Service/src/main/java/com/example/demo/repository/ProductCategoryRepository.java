package com.example.demo.repository;

import com.example.demo.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    @Query("SELECT pc FROM ProductCategory pc WHERE pc.product.productId = :productId")
    List<ProductCategory> findByProductId(@Param("productId") Long productId);

    @Query("SELECT pc FROM ProductCategory pc WHERE pc.category.categoryId = :categoryId")
    List<ProductCategory> findByCategoryId(@Param("categoryId") Long categoryId);
}
