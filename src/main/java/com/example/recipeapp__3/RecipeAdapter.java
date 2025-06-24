package com.example.recipeapp__3;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipeList;
    private RecipeViewHolder.OnFavoriteClickListener favoriteClickListener;
    private RecipeViewHolder.OnEditClickListener editClickListener; // New listener for edit
    private RecipeViewHolder.OnDeleteClickListener deleteClickListener;

    // Constructor to initialize the recipe list and listeners
    public RecipeAdapter(List<Recipe> recipeList,
                         RecipeViewHolder.OnFavoriteClickListener favoriteClickListener,
                         RecipeViewHolder.OnEditClickListener editClickListener,
                         RecipeViewHolder.OnDeleteClickListener deleteClickListener) {
        this.recipeList = recipeList;
        this.favoriteClickListener = favoriteClickListener;
        this.editClickListener = editClickListener;
        this.deleteClickListener = deleteClickListener; // Initialize delete listener
    }


    public void updateRecipes(List<Recipe> newRecipes) {
        this.recipeList = newRecipes;
        notifyDataSetChanged();
    }

    public void updateRecipeList(List<Recipe> newRecipeList) {
        this.recipeList = newRecipeList;
        notifyDataSetChanged(); // Notify the adapter to update the RecyclerView
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the item layout (item_recipe.xml)
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        // Get the current recipe
        Recipe recipe = recipeList.get(position);

        // Bind data to the views
        holder.titleTextView.setText(recipe.getTitle());

        // Bind the imageUrl to the recipeImageView using Glide
        Glide.with(holder.itemView.getContext())
                .load(recipe.getImageUrl()) // Pass the image URL from the recipe object
                .into(holder.recipeImageView);

        // Set favorite icon based on the favorite status
        holder.favImageView.setImageResource(recipe.isFavorite() ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);

        // Set OnClickListener for favorite icon
        holder.favImageView.setOnClickListener(v -> {
            if (favoriteClickListener != null) {
                favoriteClickListener.onFavoriteClick(recipe);
            }
        });

        // Set OnClickListener for the Edit button
        holder.editRecipeButton.setOnClickListener(v -> {
            if (editClickListener != null) {
                editClickListener.onEditClick(recipe);  // Handle Edit action
            }
        });

        // Set OnClickListener for the Delete button
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(recipe); // Handle delete action
            }
        });

        // Bind ingredients and steps
        String ingredients = String.join(", ", recipe.getIngredients());
        holder.ingredientsTextView.setText("Ingredients: " + ingredients);

        String steps = String.join("\n", recipe.getSteps());
        holder.stepsTextView.setText("Steps: " + steps);
    }

    @Override
    public int getItemCount() {
        return recipeList.size();  // Return the size of the recipe list
    }

    // ViewHolder class to hold references to the views in each item
    public static class RecipeViewHolder extends RecyclerView.ViewHolder {

        public TextView titleTextView;
        public ImageView recipeImageView;
        public TextView ingredientsTextView;
        public TextView stepsTextView;
        public ImageView favImageView;
        public ImageView editRecipeButton; // Button to edit recipe
        public ImageView deleteButton;

        @SuppressLint("WrongViewCast")
        public RecipeViewHolder(View view) {
            super(view);
            titleTextView = view.findViewById(R.id.recipeTitle);
            recipeImageView = view.findViewById(R.id.recipeImage); // ImageView for the image
            ingredientsTextView = view.findViewById(R.id.ingredientsText);
            stepsTextView = view.findViewById(R.id.stepsText);
            favImageView = view.findViewById(R.id.favoriteIcon); // Favorite icon ImageView
            editRecipeButton = view.findViewById(R.id.editButton); // Edit Button
            deleteButton = view.findViewById(R.id.deleteButton);
        }

        // Interface for favorite click listener
        public interface OnFavoriteClickListener {
            void onFavoriteClick(Recipe recipe);
        }

        // Interface for edit click listener
        public interface OnEditClickListener {
            void onEditClick(Recipe recipe);
        }
        public interface OnDeleteClickListener {
            void onDeleteClick(Recipe recipe);
        }


    }
}
