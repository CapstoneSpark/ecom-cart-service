//package com.example.model;
//
//import com.fasterxml.jackson.annotation.JsonManagedReference;
//import jakarta.persistence.*;
//import java.util.ArrayList;
//import java.util.List;
//
//@Entity
//@Table(name = "cart")
//public class Cart {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(unique = true, nullable = false)
//    private String userId;
//
//    @OneToMany(
//        mappedBy = "cart",
//        cascade = CascadeType.ALL,
//        orphanRemoval = true,
//        fetch = FetchType.EAGER
//    )
//    @JsonManagedReference
//    private List<CartItem> items = new ArrayList<>();
//
//    @Transient
//    private double totalAmount;
//
//    public Cart() {}
//
//    public Cart(Long id, String userId, List<CartItem> items, double totalAmount) {
//        this.id = id;
//        this.userId = userId;
//        this.items = items;
//        this.totalAmount = totalAmount;
//    }
//
//    public void calculateTotal() {
//        this.totalAmount = this.items.stream()
//            .mapToDouble(item -> item.getPrice() * item.getQuantity())
//            .sum();
//    }
//
//    // getters & setters
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//    public String getUserId() { return userId; }
//    public void setUserId(String userId) { this.userId = userId; }
//    public List<CartItem> getItems() { return items; }
//    public void setItems(List<CartItem> items) { this.items = items; }
//    public double getTotalAmount() { return totalAmount; }
//    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
//}


package com.example.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cart")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String userId;

    @OneToMany(
        mappedBy = "cart",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.EAGER
    )
    @JsonManagedReference
    private List<CartItem> items = new ArrayList<>();

    @Transient
    private double totalAmount;

    public Cart() {}

    public Cart(Long id, String userId, List<CartItem> items, double totalAmount) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.totalAmount = totalAmount;
    }

    public void calculateTotal() {
        this.totalAmount = this.items.stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
}
