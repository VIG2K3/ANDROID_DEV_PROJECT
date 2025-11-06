package com.example.kohasignuplogin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DonationCategoriesFragment extends Fragment {

    private static final String TAG = "DonationCategories"; // 🔹 Tag for Logcat
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private DonationCategoryAdapter adapter;
    private List<DonationCategory> categoryList = new ArrayList<>();
    private TextView textViewData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called"); // 🔹 Debug
        return inflater.inflate(R.layout.fragment_donation_categories, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called"); // 🔹 Debug

        recyclerView = view.findViewById(R.id.recyclerViewCategories);
        textViewData = view.findViewById(R.id.textViewData);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DonationCategoryAdapter(categoryList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        Log.d("FIREBASE_CHECK", db.getApp().getOptions().getProjectId());
        Log.d(TAG, "Firestore initialized: " + (db != null)); // 🔹 Debug

        loadDonationCategories();
    }

    private void loadDonationCategories() {
        Log.d(TAG, "loadDonationCategories() started"); // 🔹 Debug
        textViewData.setVisibility(View.VISIBLE);
        textViewData.setText("Loading donation categories...");

        db.collection("donation_categories")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Query success, document count: " + queryDocumentSnapshots.size()); // 🔹 Debug

                    categoryList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Log.d(TAG, "Document ID: " + document.getId());
                        Log.d(TAG, "Document data: " + document.getData());

                        try {
                            DonationCategory category = document.toObject(DonationCategory.class);
                            categoryList.add(category);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing document: " + e.getMessage(), e);
                        }
                    }

                    // ✅ Always update the list and hide the loading text
                    adapter.notifyDataSetChanged();

                    if (categoryList.isEmpty()) {
                        textViewData.setVisibility(View.VISIBLE);
                        textViewData.setText("No categories found.");
                        Log.w(TAG, "Parsed list empty even though documents exist");
                    } else {
                        textViewData.setVisibility(View.GONE); // ✅ This ensures RecyclerView is visible
                        Log.d(TAG, "Categories loaded successfully, count: " + categoryList.size());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading data from Firestore", e);
                    textViewData.setVisibility(View.VISIBLE);
                    textViewData.setText("Error loading data: " + e.getMessage());
                });
    }
}
