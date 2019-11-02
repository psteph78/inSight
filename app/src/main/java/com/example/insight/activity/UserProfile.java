package com.example.insight.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.insight.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfile extends AppCompatActivity {

    private TextView userName;
    private TextView userPoints;
    private TextView firstEntry;
    private TextView secondEntry;
    private TextView thirdEntry;
    private TextView firstEntryPoints;
    private TextView secondEntryPoints;
    private TextView thirdEntryPoints;
    private CircleImageView profilePicture;

    private Button logoutButton;
    private Button userProfileButton;
    private Button mapViewButton;


    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userName = findViewById(R.id.userNameText);
        userPoints = findViewById(R.id.bonusPointsText);
        firstEntry = findViewById(R.id.firstEntryName);
        secondEntry = findViewById(R.id.secondEntryName);
        thirdEntry = findViewById(R.id.thirdEntryName);
        firstEntryPoints = findViewById(R.id.firstEntryPoints);
        secondEntryPoints = findViewById(R.id.secondEntryPoints);
        thirdEntryPoints = findViewById(R.id.thirdEntryPoints);
        profilePicture = findViewById(R.id.profile_image);
        logoutButton = findViewById(R.id.logoutButton);
        userProfileButton = findViewById(R.id.userProfileButton);
        mapViewButton = findViewById(R.id.mapViewButton);

        firstEntryPoints.setVisibility(View.INVISIBLE);
        secondEntryPoints.setVisibility(View.INVISIBLE);
        thirdEntryPoints.setVisibility(View.INVISIBLE);
        firstEntry.setVisibility(View.INVISIBLE);
        secondEntry.setVisibility(View.INVISIBLE);
        thirdEntry.setVisibility(View.INVISIBLE);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String email = currentUser.getEmail();

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("users");

        databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()){

                    userName.setText(childDataSnapshot.child("username").getValue().toString());

                    Spannable nrPoints = new SpannableString(childDataSnapshot.child("points").getValue().toString());
                    nrPoints.setSpan(new ForegroundColorSpan(Color.rgb(23,104,120)), 0, nrPoints.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    nrPoints.setSpan(new RelativeSizeSpan(1.8f), 0, nrPoints.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    nrPoints.setSpan(new StyleSpan(Typeface.BOLD), 0, nrPoints.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    userPoints.setText(nrPoints);
                    userPoints.append("\n");

                    Spannable points = new SpannableString("total points");
                    points.setSpan(new ForegroundColorSpan(Color.rgb(128,128,128)), 0, points.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    points.setSpan(new RelativeSizeSpan(0.7f), 0, points.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    userPoints.append(points);

                    String profileImgUrl = childDataSnapshot.child("profileImgUrl").getValue().toString();
                    Picasso.get().load(profileImgUrl).into(profilePicture);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(UserProfile.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        userProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //DOESN'T DO ANYTHING; ALREADY ON USER PROFILE
                startActivity(new Intent(UserProfile.this, MainActivity.class));
            }
        });

        mapViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //WILL GO TO MAP
                //startActivity(new Intent(UserProfile.this, MainActivity.class));
            }
        });
    }
}
