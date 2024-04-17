package com.example.rey;

public class Order {
    String pickupLocation;

    public Order(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    @Override
    public String toString() {
        return "Pickup Location: " + pickupLocation;
    }
}
