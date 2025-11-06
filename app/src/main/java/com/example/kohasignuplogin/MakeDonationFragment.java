package com.example.kohasignuplogin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class MakeDonationFragment extends Fragment {

    private StorageReference storageReference;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    private ImageView photoIcon;

    // Activity Result Launcher for picking image
    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // No general storage ref, we'll build paths dynamically with user UID on upload

        // Register the image picker callback
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        photoIcon.setImageURI(uri);
                        uploadImageToFirebase(uri);
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_make_donation, container, false);

        // Back arrow setup (assuming same as before)
        ImageView backArrow = view.findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> {
            HomeFragment homeFragment = new HomeFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in_left,
                            R.anim.slide_out_left,
                            R.anim.slide_in_left,
                            R.anim.slide_out_left
                    )
                    .replace(R.id.frame_layout, homeFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Photo icon
        photoIcon = view.findViewById(R.id.photo_icon);
        photoIcon.setOnClickListener(v -> openGallery());

        return view;
    }

    // Open gallery using Activity Result API
    private void openGallery() {
        pickImageLauncher.launch("image/*");
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri == null) return;

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        String fileName = System.currentTimeMillis() + ".jpg";

        // Store image under user-specific folder
        storageReference = FirebaseStorage.getInstance()
                .getReference()
                .child("donation_images")
                .child(uid)
                .child(fileName);

        storageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl()
                        .addOnSuccessListener(uri -> saveImageUrlToFirestore(uri.toString(), uid)))
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveImageUrlToFirestore(String url, String uid) {
        Map<String, Object> data = new HashMap<>();
        data.put("imageUrl", url);
        data.put("userId", uid);
        data.put("timestamp", System.currentTimeMillis());

        firestore.collection("donations")
                .add(data)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(getContext(), "Image uploaded and saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to save URL: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
