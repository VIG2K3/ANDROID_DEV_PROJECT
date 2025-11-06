package com.example.kohasignuplogin;

public class DonationCategory {
    private String category;
    private String description;
    private int points;

    public DonationCategory() {
        // Firestore requires an empty constructor
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public int getPoints() {
        return points;
    }
}
