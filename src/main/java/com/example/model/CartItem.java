
package com.example.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "cart_item")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Back reference to avoid infinite recursion
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonBackReference
    private Cart cart;

    // Product SKU (matches product table)
    @Column(name = "sku", nullable = false)
    private String sku;

    // Product ID (numeric ID from product table)
    @Column(name = "product_id")
    private Long productId;

    private String name;
    private double price;
    private int quantity;

    // EXTRA FIELDS FOR UI DISPLAY
    private String imageUrl;   // ⭐ REQUIRED FOR CART IMAGE
    private String brand;      // optional but useful

    public CartItem() {}

    public CartItem(Long id, Cart cart, String sku, Long productId,
                    String name, String brand, String imageUrl,
                    double price, int quantity) {
        this.id = id;
        this.cart = cart;
        this.sku = sku;
        this.productId = productId;
        this.name = name;
        this.brand = brand;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
