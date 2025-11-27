//package com.example.model;
//
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import jakarta.persistence.*;
//
//@Entity
//@Table(name = "cart_item")
//public class CartItem {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    // back reference to avoid infinite recursion in JSON
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "cart_id", nullable = false)
//    @JsonBackReference
//    private Cart cart;
//
//    // SKU string (matches product.sku)
//    @Column(name = "product_id", nullable = false)
//    private String productId;
//
//    private String name;
//    private double price;
//    private int quantity;
//
//    public CartItem() {}
//
//    public CartItem(Long id, Cart cart, String productId, String name, double price, int quantity) {
//        this.id = id;
//        this.cart = cart;
//        this.productId = productId;
//        this.name = name;
//        this.price = price;
//        this.quantity = quantity;
//    }
//
//    
//  
//
//    // getters & setters
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//    public Cart getCart() { return cart; }
//    public void setCart(Cart cart) { this.cart = cart; }
//    public String getProductId() { return productId; }
//    public void setProductId(String productId) { this.productId = productId; }
//    public String getName() { return name; }
//    public void setName(String name) { this.name = name; }
//    public double getPrice() { return price; }
//    public void setPrice(double price) { this.price = price; }
//    public int getQuantity() { return quantity; }
//    public void setQuantity(int quantity) { this.quantity = quantity; }
//}



package com.example.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "cart_item")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonBackReference
    private Cart cart;

    @Column(name = "sku", nullable = false)
    private String sku;

    private String name;
    private double price;
    private int quantity;

    public CartItem() {}

    public CartItem(Long id, Cart cart, String sku, String name, double price, int quantity) {
        this.id = id;
        this.cart = cart;
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
