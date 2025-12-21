package com.cinema.models;

public class ComboOrderItem {
    private FoodCombo foodCombo;
    private int quantity;

    public ComboOrderItem() {
        this.quantity = 0;
    }

    public ComboOrderItem(FoodCombo foodCombo, int quantity) {
        this.foodCombo = foodCombo;
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return foodCombo.getPrice() * quantity;
    }

    // Getters and Setters
    public FoodCombo getFoodCombo() { return foodCombo; }
    public void setFoodCombo(FoodCombo foodCombo) { this.foodCombo = foodCombo; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public void incrementQuantity() { this.quantity++; }
    public void decrementQuantity() { 
        if (this.quantity > 0) {
            this.quantity--; 
        }
    }
}
