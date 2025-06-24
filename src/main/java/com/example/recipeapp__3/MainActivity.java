package com.example.recipeapp__3;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipeList;
    private FirebaseFirestore db;
    private FirebaseUser user;

    private Button signInButton, signUpButton;
    private ImageView addRecipeButton;
    private ImageView viewFavButton;
    private ImageView buttonSignOut;
    private boolean isFavoriteView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Firestore
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        // Get the current user
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // If user is not logged in, redirect to login screen
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        // Initialize views
        buttonSignOut = findViewById(R.id.buttonSignOut);
        recyclerView = findViewById(R.id.recipeRecyclerView);
        viewFavButton = findViewById(R.id.viewFavButton);
        addRecipeButton = findViewById(R.id.addRecipeButton);
        signInButton = findViewById(R.id.signInButton);
        signUpButton = findViewById(R.id.signUpButton);

        // Set the OnClickListener for the sign-out button
        buttonSignOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
            finish();
        });

        // Initialize RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the list of recipes and adapter
        recipeList = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(
                recipeList,
                this::toggleFavorite,
                this::showEditRecipeDialog,
                this::deleteRecipe
        );
        recyclerView.setAdapter(recipeAdapter);

        // Set up View Favorites button
        viewFavButton.setOnClickListener(v -> {
            if (isFavoriteView) {
                recipeAdapter.updateRecipeList(recipeList); // Show all recipes
            } else {
                List<Recipe> favoriteRecipes = getFavoriteRecipes();
                recipeAdapter.updateRecipeList(favoriteRecipes); // Show only favorites
            }
            isFavoriteView = !isFavoriteView;
        });

        // Load recipes from Firestore
        loadRecipesFromFirestore();

        // Set up Add Recipe button
        addRecipeButton.setOnClickListener(v -> showAddRecipeDialog());

        // Set up Sign In button
        signInButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SignInActivity.class)));

        // Set up Sign Up button
        signUpButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SignUpActivity.class)));
    }

    private void loadRecipesFromFirestore() {
        String userId = user.getUid();
        db.collection("users").document(userId).collection("recipelist")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        recipeList.clear(); // Clear the list to avoid duplicates
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Recipe recipe = document.toObject(Recipe.class);
                            if (recipe != null) {
                                recipe.setId(document.getId()); // Set document ID for updates
                                recipeList.add(recipe);
                            }
                        }
                        recipeAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "No recipes found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error getting recipes: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showAddRecipeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_recipe, null);
        builder.setView(dialogView);

        EditText inputTitle = dialogView.findViewById(R.id.inputRecipeTitle);
        EditText inputImageUrl = dialogView.findViewById(R.id.inputRecipeImageUrl);
        EditText inputIngredients = dialogView.findViewById(R.id.inputRecipeIngredients);
        EditText inputSteps = dialogView.findViewById(R.id.inputRecipeSteps);
        Button saveButton = dialogView.findViewById(R.id.saveRecipeButton);

        AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(v -> {
            String title = inputTitle.getText().toString().trim();
            String imageUrl = inputImageUrl.getText().toString().trim();
            String ingredientsStr = inputIngredients.getText().toString().trim();
            String stepsStr = inputSteps.getText().toString().trim();

            if (title.isEmpty() || imageUrl.isEmpty() || ingredientsStr.isEmpty() || stepsStr.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> ingredients = Arrays.asList(ingredientsStr.split(","));
            List<String> steps = Arrays.asList(stepsStr.split(","));

            Recipe recipe = new Recipe();
            recipe.setTitle(title);
            recipe.setImageUrl(imageUrl);
            recipe.setIngredients(ingredients);
            recipe.setSteps(steps);

            addRecipeToFirestore(recipe);

            dialog.dismiss();
        });

        dialog.show();
    }

    private void showEditRecipeDialog(Recipe recipe) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_recipe, null); // Reuse the same layout for add/edit
        builder.setView(dialogView);

        EditText inputTitle = dialogView.findViewById(R.id.inputRecipeTitle);
        EditText inputImageUrl = dialogView.findViewById(R.id.inputRecipeImageUrl);
        EditText inputIngredients = dialogView.findViewById(R.id.inputRecipeIngredients);
        EditText inputSteps = dialogView.findViewById(R.id.inputRecipeSteps);
        Button saveButton = dialogView.findViewById(R.id.saveRecipeButton);

        inputTitle.setText(recipe.getTitle());
        inputImageUrl.setText(recipe.getImageUrl());
        inputIngredients.setText(String.join(",", recipe.getIngredients()));
        inputSteps.setText(String.join(",", recipe.getSteps()));

        AlertDialog dialog = builder.create();

        saveButton.setText("Update Recipe");

        saveButton.setOnClickListener(v -> {
            String title = inputTitle.getText().toString().trim();
            String imageUrl = inputImageUrl.getText().toString().trim();
            String ingredientsStr = inputIngredients.getText().toString().trim();
            String stepsStr = inputSteps.getText().toString().trim();

            if (title.isEmpty() || imageUrl.isEmpty() || ingredientsStr.isEmpty() || stepsStr.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> ingredients = Arrays.asList(ingredientsStr.split(","));
            List<String> steps = Arrays.asList(stepsStr.split(","));

            recipe.setTitle(title);
            recipe.setImageUrl(imageUrl);
            recipe.setIngredients(ingredients);
            recipe.setSteps(steps);

            updateRecipeInFirestore(recipe);

            dialog.dismiss();
        });

        dialog.show();
    }

    private void addRecipeToFirestore(Recipe recipe) {
        String userId = user.getUid();
        recipe.setFavorite(false);

        db.collection("users").document(userId).collection("recipelist")
                .add(recipe)
                .addOnSuccessListener(documentReference -> {
                    recipe.setId(documentReference.getId());
                    recipeList.add(recipe);
                    recipeAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Recipe added successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add recipe: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateRecipeInFirestore(Recipe recipe) {
        String userId = user.getUid();

        db.collection("users").document(userId).collection("recipelist")
                .document(recipe.getId())
                .set(recipe)
                .addOnSuccessListener(aVoid -> {
                    int index = recipeList.indexOf(recipe);
                    if (index != -1) {
                        recipeList.set(index, recipe);
                        recipeAdapter.notifyItemChanged(index);
                    }
                    Toast.makeText(this, "Recipe updated successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update recipe: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteRecipe(Recipe recipe) {
        String userId = user.getUid();

        db.collection("users").document(userId).collection("recipelist")
                .document(recipe.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    recipeList.remove(recipe);
                    recipeAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Recipe deleted successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete recipe: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void toggleFavorite(Recipe recipe) {
        recipe.setFavorite(!recipe.isFavorite());
        updateRecipeInFirestore(recipe);
    }

    private List<Recipe> getFavoriteRecipes() {
        List<Recipe> favoriteRecipes = new ArrayList<>();
        for (Recipe recipe : recipeList) {
            if (recipe.isFavorite()) {
                favoriteRecipes.add(recipe);
            }
        }
        return favoriteRecipes;
    }
}
