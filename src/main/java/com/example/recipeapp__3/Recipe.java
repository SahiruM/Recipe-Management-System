package com.example.recipeapp__3;

import java.util.List;

public class Recipe {
    private String id;
    private String title;
    private String imageUrl;
    private List<String> ingredients;
    private List<String> steps;
    private boolean favorite;
    // Add a constructor, getters, and setters

    public Recipe() {
        // Empty constructor needed for Firestore to create an object
    }

    public Recipe(String title, String imageUrl, boolean favorite, List<String> ingredients, List<String> steps) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.ingredients = ingredients;
        this.steps = steps;
        this.favorite = favorite;
    }
    public String getId(){ return id;}
    public void setId(String id){ this.id= id;}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public List<String> getSteps() {
        return steps;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }


}
