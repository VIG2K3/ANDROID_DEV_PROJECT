package com.example.navbotdialog;

import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RewardCard extends AppCompatActivity {

    private TextView rewardDescription;
    private Button redeemButton;
    private DatabaseReference userPointsRef;
    private long userPoints = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_dash);

        TextView rewardTitle = findViewById(R.id.clothTitle);
        ImageView rewardImage = findViewById(R.id.clothImage);
        rewardDescription = findViewById(R.id.rewardDescription);
        redeemButton = findViewById(R.id.redeemButton);

        // Get data from intent
        String name = getIntent().getStringExtra("reward_name");
        int imageRes = getIntent().getIntExtra("reward_image", R.drawable.kfc);

        rewardTitle.setText(name);
        rewardImage.setImageResource(imageRes);

        // Setup required points for each reward
        final long[] requiredPoints = {0}; // make effectively final for lambda
        String description = "";

        switch (name) {
            case "KFC (100 KO-Points)":
                requiredPoints[0] = 100;
                description = "<b>CRAVING SOMETHING DELICIOUS?</b><br><br>" +
                        "Your favourite meal perfect for lunch, dinner, or even a quick snack. Note: Voucher will expire in 30 days after redeeming!";
                redeemButton.setText("Redeem for 100 KO-Points");
                break;
            case "TNG (500 KO-Points)":
                requiredPoints[0] = 500;
                description = "<b>STAY CASHLESS AND CONVENIENT!</b><br><br>" +
                        "Use your KO-Points to redeem a Touch 'n Go eWallet top-up worth RM10. Note: Voucher will expire in 30 days after redeeming!";
                redeemButton.setText("Redeem for 500 KO-Points");
                break;
            case "XOX (200 KO-Points)":
                requiredPoints[0] = 200;
                description = "<b>RUNNING LOW ON MOBILE CREDIT?</b><br><br>" +
                        "Redeem your XOX prepaid top-up and stay connected. Note: Voucher will expire in 30 days after redeeming!";
                redeemButton.setText("Redeem for 200 KO-Points");
                break;
            case "GRAB (300 KO-Points)":
                requiredPoints[0] = 300;
                description = "<b>GO ANYWHERE, EAT ANYTHING!</b><br><br>" +
                        "Redeem this Grab voucher for rides or food delivery. Note: Voucher will expire in 30 days after redeeming!";
                redeemButton.setText("Redeem for 300 KO-Points");
                break;
            case "DIY (400 KO-Points)":
                requiredPoints[0] = 400;
                description = "<b>TIME TO GET CREATIVE!</b><br><br>" +
                        "Redeem a MR.DIY shopping voucher. Note: Voucher will expire in 30 days after redeeming!";
                redeemButton.setText("Redeem for 400 KO-Points");
                break;
            case "LOTUS (400 KO-Points)":
                requiredPoints[0] = 400;
                description = "<b>SHOP MORE, SAVE MORE!</b><br><br>" +
                        "Use your KO-Points to redeem a Lotus’s shopping voucher. Note: Voucher will expire in 30 days after redeeming!";
                redeemButton.setText("Redeem for 400 KO-Points");
                break;
        }

        rewardDescription.setText(Html.fromHtml(description));
        rewardDescription.setTextSize(20);
        rewardDescription.setLineSpacing(6, 1);
        rewardDescription.setGravity(Gravity.CENTER);

        // --- Firebase: get current user's points ---
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            userPointsRef = FirebaseDatabase.getInstance(
                            "https://koha-user-points.asia-southeast1.firebasedatabase.app/")
                    .getReference("users")
                    .child(uid)
                    .child("points");

            // Initialize points if null
            userPointsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Long points = snapshot.getValue(Long.class);
                    if (points != null) {
                        userPoints = points;
                    } else {
                        userPoints = 0L;
                        userPointsRef.setValue(userPoints); // initialize in DB
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });

            // Listen for live updates
            userPointsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Long points = snapshot.getValue(Long.class);
                    if (points != null) {
                        userPoints = points;
                    } else {
                        userPoints = 0L;
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        }

        // --- Redeem button logic ---
        redeemButton.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(RewardCard.this)
                    .setTitle("Confirm Redemption")
                    .setMessage("Are you sure you want to redeem this voucher for "
                            + requiredPoints[0] + " KO-Points?")
                    .setPositiveButton("Yes", (dialog, which) -> {

                        if (userPoints >= requiredPoints[0]) {
                            long newPoints = userPoints - requiredPoints[0];

                            userPointsRef.setValue(newPoints).addOnCompleteListener(task -> {
                                String message;
                                if (task.isSuccessful()) {
                                    message = "You have spent " + requiredPoints[0] +
                                            " KO-Points.\nThank you for your contribution!\n\nYour voucher code is <<KOHA123>>.\nVoucher will expire in 30 days.";
                                } else {
                                    message = "Error updating points. Try again.";
                                }

                                // Show pop-up dialog with the message
                                new androidx.appcompat.app.AlertDialog.Builder(RewardCard.this)
                                        .setTitle("Redemption Successful!")
                                        .setMessage(message)
                                        .setPositiveButton("OK", (d, w) -> d.dismiss())
                                        .show();
                            });
                        } else {
                            // Not enough points pop-up
                            new androidx.appcompat.app.AlertDialog.Builder(RewardCard.this)
                                    .setTitle("Redemption Failed")
                                    .setMessage("\nNot enough KO-Points to Redeem Voucher!")
                                    .setPositiveButton("OK", (d, w) -> d.dismiss())
                                    .show();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }
}
