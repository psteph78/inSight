package com.example.insight.activity.userActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.insight.R;
import com.example.insight.activity.MainActivity;
import com.example.insight.activity.mapActivity.MapsActivity;
import com.example.insight.entity.CommentForLocation;
import com.example.insight.entity.PictureForLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserPictures extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    private Button mapViewButton;
    private Button userProfileButton;

    private List<PictureForLocation> allUserPictures;

    private Dialog userOptionsDialog;
    private Dialog logOutDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_pictures);

        userOptionsDialog = new Dialog(this);
        logOutDialog = new Dialog(this);
        mapViewButton = findViewById(R.id.mapViewButton);
        userProfileButton = findViewById(R.id.userProfileButton);

        retrieveAllUserPictures();

        userProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserOptionMenu();
            }
        });

        mapViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserPictures.this, MapsActivity.class));
            }
        });
    }

    /**
     * method retrieves all pictures
     * of user from db
     */
    private void retrieveAllUserPictures() {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("locationPictures");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String email = currentUser.getEmail();

        databaseReference.orderByChild("userEmail").equalTo(email).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allUserPictures = new ArrayList<>();
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()){
                    PictureForLocation pictureForLocation = new PictureForLocation();
                    pictureForLocation.setLocationName(childDataSnapshot.child("locationName").getValue().toString());
                    pictureForLocation.setEncodedPicture(childDataSnapshot.child("encodedPicture").getValue().toString());
                    allUserPictures.add(pictureForLocation);
                }

                showUserPicturesInUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * method shows all user pictures
     * in UI
     */
    private void showUserPicturesInUI() {
        LinearLayout userPicturesView;
        userPicturesView = findViewById(R.id.user_pictures_view);

        LinearLayout.LayoutParams paramsForPictureWithLocation = new LinearLayout.LayoutParams(675, 570);
        paramsForPictureWithLocation.setMargins(15, 0, 0, 0);

        LinearLayout.LayoutParams paramsForPicture = new LinearLayout.LayoutParams(665, 510);
        paramsForPicture.setMargins(8, 0, 0, 0);

        LinearLayout.LayoutParams paramsForPictureRow = new LinearLayout.LayoutParams(1400, 600);
        paramsForPictureRow.setMargins(0, 0, 0, 35);
        LinearLayout pictureRow = new LinearLayout(this);
        pictureRow.setOrientation(LinearLayout.HORIZONTAL);
        pictureRow.setLayoutParams(paramsForPictureRow);

        for(int i=0; i<allUserPictures.size(); i++){
            LinearLayout pictureWithLocation = new LinearLayout(this);
            pictureWithLocation.setOrientation(LinearLayout.VERTICAL);
            pictureWithLocation.setLayoutParams(paramsForPictureWithLocation);

            //location title
            TextView locationNameText = new TextView(this);
            Spannable locationName = new SpannableString(allUserPictures.get(i).getLocationName());
            locationName.setSpan(new StyleSpan(Typeface.ITALIC), 0, locationName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            locationName.setSpan(new ForegroundColorSpan(Color.rgb(23,104,120)), 0, locationName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            locationName.setSpan(new RelativeSizeSpan(1.05f), 0, locationName.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            locationNameText.setText(locationName);

            //location user picture
            ImageView userPicture = new ImageView(this);
            byte[] decodedString = Base64.decode(allUserPictures.get(i).getEncodedPicture(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            userPicture.setImageBitmap(Bitmap.createScaledBitmap(decodedByte, 650, 500, false));
            userPicture.setLayoutParams(paramsForPicture);
            pictureWithLocation.addView(userPicture);
            pictureWithLocation.addView(locationNameText);

            if (i != 0 && (i + 1) % 2 == 0) {
                pictureRow.addView(pictureWithLocation);
                userPicturesView.addView(pictureRow);

                pictureRow = new LinearLayout(this);
                pictureRow.setOrientation(LinearLayout.HORIZONTAL);
                pictureRow.setLayoutParams(paramsForPictureRow);
            } else if (i == allUserPictures.size() - 1) {
                pictureRow.addView(pictureWithLocation);
                userPicturesView.addView(pictureRow);
            } else {
                pictureRow.addView(pictureWithLocation);
            }
        }

    }

    /**
     * method displays menu for user profile options
     */
    public void showUserOptionMenu(){
        TextView userCommentsView;
        TextView userProfileView;
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
        userCommentsView = userOptionsDialog.findViewById(R.id.userCommentsBtn);
        exchangePointsView = userOptionsDialog.findViewById(R.id.exchangePointsBtn);
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
                startActivity(new Intent(UserPictures.this, UserProfile.class));
            }
        });

        userCommentsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserPictures.this, UserComments.class));
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
                Intent intent = new Intent(UserPictures.this, MainActivity.class);
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
