package com.example.demo.repository;

import com.example.demo.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :kw, '%'))")
    List<Product> searchByKeyword(@Param("kw") String keyword);
 // In ProductRepository.java
    Optional<Product> findBySku(String sku);
}
