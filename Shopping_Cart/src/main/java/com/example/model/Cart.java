package com.example.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cart")
@Data
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String userId;

    @OneToMany(
        mappedBy = "cart",
        cascade = CascadeType.ALL,
        orphanRemoval = true, // Crucial for deleting items when removed from this list
        fetch = FetchType.EAGER // <--- CHANGE THIS
    )
    private List<CartItem> items = new ArrayList<>();

    // Calculate and update the total amount based on current items
    @Transient // This field is not stored in the database
    private double totalAmount = 0.0; 

    public void calculateTotal() {
        // Use a Stream to iterate over items and sum the subtotals (price * quantity)
        this.totalAmount = this.items.stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public List<CartItem> getItems() {
		return items;
	}

	public void setItems(List<CartItem> items) {
		this.items = items;
	}

	public double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}
	

	public Cart(Long id, String userId, List<CartItem> items, double totalAmount) {
		super();
		this.id = id;
		this.userId = userId;
		this.items = items;
		this.totalAmount = totalAmount;
	}

	public Cart() {
	}
    
    
    // Lombok provides Getters, Setters, etc.
}
