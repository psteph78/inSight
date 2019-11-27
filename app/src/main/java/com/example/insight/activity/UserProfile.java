package com.example.insight.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.insight.R;
import com.example.insight.activity.mapActivity.MapsActivity;
import com.example.insight.entity.VisitedLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

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
    private List<VisitedLocation> visitedLocations;

    private Button mapViewButton;
    private Button userProfileButton;


    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    private Dialog userOptionsDialog;
    private Dialog logOutDialog;


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
        mapViewButton = findViewById(R.id.mapViewButton);
        userProfileButton = findViewById(R.id.userProfileButton);

        firstEntryPoints.setVisibility(View.INVISIBLE);
        secondEntryPoints.setVisibility(View.INVISIBLE);
        thirdEntryPoints.setVisibility(View.INVISIBLE);
        firstEntry.setVisibility(View.INVISIBLE);
        secondEntry.setVisibility(View.INVISIBLE);
        thirdEntry.setVisibility(View.INVISIBLE);

        userOptionsDialog = new Dialog(this);
        logOutDialog = new Dialog(this);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String email = currentUser.getEmail();
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("visitedLocations");

        databaseReference.orderByChild("userEmail").equalTo(email).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                visitedLocations = new ArrayList<>();
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()){
                    VisitedLocation visitedLocation = new VisitedLocation();
                    visitedLocation.setLocationName(childDataSnapshot.child("locationName").getValue().toString());
                    visitedLocation.setPoints(Integer.valueOf(childDataSnapshot.child("points").getValue().toString()));

                    visitedLocations.add(visitedLocation);
                }
                showVisitedLocationsInUi();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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

        userProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserOptionMenu();
            }
        });

        mapViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserProfile.this, MapsActivity.class));
            }
        });
    }

    private void showVisitedLocationsInUi() {
        if (visitedLocations != null && visitedLocations.size()>0){
            firstEntry.setText(visitedLocations.get(visitedLocations.size()-1).getLocationName());
            addLocationPointsToUI(firstEntryPoints, String.valueOf(visitedLocations.get(visitedLocations.size()-1).getPoints()));
            firstEntryPoints.setVisibility(View.VISIBLE);
            firstEntry.setVisibility(View.VISIBLE);

            if (visitedLocations.size() > 1){
                secondEntry.setText(visitedLocations.get(visitedLocations.size()-2).getLocationName());
                addLocationPointsToUI(secondEntryPoints, String.valueOf(visitedLocations.get(visitedLocations.size()-2).getPoints()));
                secondEntryPoints.setVisibility(View.VISIBLE);
                secondEntry.setVisibility(View.VISIBLE);
            }

            if(visitedLocations.size() > 2){
                thirdEntry.setText(visitedLocations.get(visitedLocations.size()-3).getLocationName());
                addLocationPointsToUI(thirdEntryPoints, String.valueOf(visitedLocations.get(visitedLocations.size()-3).getPoints()));
                thirdEntryPoints.setVisibility(View.VISIBLE);
                thirdEntry.setVisibility(View.VISIBLE);
            }
        }
    }

    private void addLocationPointsToUI(TextView locationPoints, String rewardPoints){
        Spannable nrPoints = new SpannableString(rewardPoints);
        nrPoints.setSpan(new ForegroundColorSpan(Color.rgb(23,104,120)), 0, nrPoints.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        nrPoints.setSpan(new RelativeSizeSpan(1.2f), 0, nrPoints.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        nrPoints.setSpan(new StyleSpan(Typeface.BOLD), 0, nrPoints.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        locationPoints.setText(nrPoints);
        locationPoints.append("\n");

        Spannable points = new SpannableString("total points");
        points.setSpan(new ForegroundColorSpan(Color.rgb(128,128,128)), 0, points.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        points.setSpan(new RelativeSizeSpan(0.75f), 0, points.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        locationPoints.append(points);
    }

    /**
     * method displays menu for user profile options
     */
    public void showUserOptionMenu(){
        TextView exchangePointsView;
        TextView logOutView;

        userOptionsDialog.setContentView(R.layout.user_options_menu);
        WindowManager.LayoutParams wmlp = userOptionsDialog.getWindow().getAttributes();

        wmlp.gravity = Gravity.TOP | Gravity.START;
        wmlp.x = 1100;
        wmlp.y = 90;
        wmlp.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        wmlp.dimAmount = 0.0f;

        exchangePointsView = userOptionsDialog.findViewById(R.id.exchangePointsBtn);
        logOutView = userOptionsDialog.findViewById(R.id.logoutBtn);


        //TODO ONCE EXCHANGE POINT ACTIVITY IS DONE
        exchangePointsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        logOutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogOutWarning();
            }
        });

        userOptionsDialog.show();
    }

    /**
     * method displays log out warning
     * and logs user out if choosen so
     */
    private void showLogOutWarning(){
        Button cancelButton;
        final Button logoutButton;

        logOutDialog.setContentView(R.layout.log_out_alert);

        cancelButton = logOutDialog.findViewById(R.id.cancelBtn);
        logoutButton = logOutDialog.findViewById(R.id.logoutBtn);

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

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOutDialog.dismiss();
            }
        });

        logOutDialog.show();
    }
}
