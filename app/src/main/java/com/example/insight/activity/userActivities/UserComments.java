package com.example.insight.activity.userActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.insight.R;
import com.example.insight.activity.MainActivity;
import com.example.insight.activity.mapActivity.MapsActivity;
import com.example.insight.entity.CommentForLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.List;

public class UserComments extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    private Button mapViewButton;
    private Button userProfileButton;

    private Dialog userOptionsDialog;
    private Dialog logOutDialog;


    private List<CommentForLocation> allUserComments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_comments);

        userOptionsDialog = new Dialog(this);
        logOutDialog = new Dialog(this);
        mapViewButton = findViewById(R.id.mapViewButton);
        userProfileButton = findViewById(R.id.userProfileButton);

        retrieveAllUserComments();

        userProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserOptionMenu();
            }
        });

        mapViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserComments.this, MapsActivity.class));
            }
        });
    }

    /**
     * method retrieves all comments of
     * current logged in user
     * from the db
     */
    private void retrieveAllUserComments(){
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("locationComments");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String email = currentUser.getEmail();

        databaseReference.orderByChild("userEmail").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allUserComments = new ArrayList<>();
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()){
                    CommentForLocation userComment = new CommentForLocation();
                    userComment.setUserComment(childDataSnapshot.child("userComment").getValue().toString());
                    userComment.setLocationName(childDataSnapshot.child("locationName").getValue().toString());
                    allUserComments.add(userComment);
                }
                showUserCommentsInUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * method displays the comments of the logged
     * in user for all locations
     */
    private void showUserCommentsInUI() {
        LinearLayout userComments;
        userComments = findViewById(R.id.user_comments_view);

        LinearLayout.LayoutParams commentParams = new LinearLayout.LayoutParams(1100, LinearLayout.LayoutParams.WRAP_CONTENT);
        commentParams.setMargins(30, 0, 0, 15);

        for (CommentForLocation comment : allUserComments) {

            //location name
            TextView locationNameText = new TextView(this);
            Spannable locationName = new SpannableString(comment.getLocationName().concat(":"));
            locationName.setSpan(new StyleSpan(Typeface.BOLD), 0, locationName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            locationName.setSpan(new ForegroundColorSpan(Color.rgb(23,104,120)), 0, locationName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            locationName.setSpan(new RelativeSizeSpan(1.1f), 0, locationName.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            locationNameText.setText(locationName);

            //user comment for location
            TextView userComment = new TextView(this);
            GradientDrawable shape = new GradientDrawable();
            shape.setCornerRadius(20);
            shape.setColor(Color.parseColor("#FFFFFF"));
            userComment.setBackground(shape);
            userComment.setLayoutParams(commentParams);
            userComment.setPadding(20, 15, 20, 40);
            userComment.setText(comment.getUserComment());

            userComments.addView(locationNameText);
            userComments.addView(userComment);
        }
    }

    /**
     * method displays menu for user profile options
     */
    public void showUserOptionMenu(){
        TextView userProfileView;
        TextView userPicturesView;
        TextView exchangePointsView;
        TextView logOutView;

        userOptionsDialog.setContentView(R.layout.user_options_menu);
        WindowManager.LayoutParams wmlp = userOptionsDialog.getWindow().getAttributes();

        wmlp.gravity = Gravity.TOP | Gravity.START;
        wmlp.x = 1100;
        wmlp.y = 110;
        wmlp.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        wmlp.dimAmount = 0.0f;

        userProfileView = userOptionsDialog.findViewById(R.id.userProfileBtn);
        exchangePointsView = userOptionsDialog.findViewById(R.id.exchangePointsBtn);
        userPicturesView = userOptionsDialog.findViewById(R.id.userPicturesBtn);
        logOutView = userOptionsDialog.findViewById(R.id.logoutBtn);


        //TODO ONCE EXCHANGE POINT ACTIVITY IS DONE
        exchangePointsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        userProfileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserComments.this, UserProfile.class));
            }
        });

        userPicturesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserComments.this, UserPictures.class));
            }
        });

        logOutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userOptionsDialog.dismiss();
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
                Intent intent = new Intent(UserComments.this, MainActivity.class);
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
