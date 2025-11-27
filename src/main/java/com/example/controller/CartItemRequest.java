//package com.example.controller;
//
//public class CartItemRequest {
//
//    private String productId; // SKU
//    private int quantity;
//
//    public CartItemRequest() {}
//
//    public CartItemRequest(String productId, int quantity) {
//        this.productId = productId;
//        this.quantity = quantity;
//    }
//
//    public String getProductId() { return productId; }
//    public void setProductId(String productId) { this.productId = productId; }
//    public int getQuantity() { return quantity; }
//    public void setQuantity(int quantity) { this.quantity = quantity; }
//}


package com.example.controller;

public class CartItemRequest {

    private String sku;
    private int quantity;

    public CartItemRequest() {}

    public CartItemRequest(String sku, int quantity) {
        this.sku = sku;
        this.quantity = quantity;
    }

    public String getSku() { 
        return sku; 
    }

    public void setSku(String sku) { 
        this.sku = sku; 
    }

    public int getQuantity() { 
        return quantity; 
    }

    public void setQuantity(int quantity) { 
        this.quantity = quantity; 
    }
}
