package com.example.insight.activity.userActivities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.insight.R;
import com.example.insight.activity.MainActivity;
import com.example.insight.activity.mapActivity.MapsActivity;
import com.example.insight.activity.userActivities.adapter.ExchangeRecyclerViewAdapter;
import com.example.insight.entity.Exchange;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ExchangePoints extends AppCompatActivity {


    private List<Exchange> allExchanges;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase fireDB;

    private Button mapViewButton;
    private Button userProfileButton;

    private Dialog userOptionsDialog;
    private Dialog logOutDialog;

    private RecyclerView recyclerView;
    private ExchangeRecyclerViewAdapter adapter;

    private void writeNewExchange() {
      //  Exchange ep = new Exchange("Ethnographic Museum", "If you have 105 points you can exchange them for 1 ticket at the museum.","105","https://www.muzeul-etnografic.ro/ro");
//        Exchange ep1 = new Exchange("Art Museum", "If you have 20 points you can exchange them for a 40% discount on an entry ticket at the art museum.","20","https://www.macluj.ro/");
//        Exchange ep2 = new Exchange("Tailors' Tower", "With 80 points you get 50% discount on an entry ticket at tailors' tower.","80","http://www.visitclujnapoca.ro/en/atractii-turistice/imi-place/page/28/the-tailors-tower.html");
//        Exchange ep3 = new Exchange("Hungarian Opera", "You get 2 entry tickets for one play at sunday with 230 points.","230","https://magyaropera.ro/");
//        Exchange ep4 = new Exchange("National Operahouse", "If you buy 2 tickets at the operahouse, you get 1 free ticket. ","170","https://www.teatrulnationalcluj.ro/");
    //    Exchange ep5 = new Exchange("Museum of History", "With 400 point you can buy 2 entry tickets at the national museum.","400","http://www.visitclujnapoca.ro/en/atractii-turistice/muzee/the-national-history-museum-of-transylvania.html");
//        Exchange ep6 = new Exchange("Evangelical Church", "You can change 10 point for one entry at the church.","10","https://ro.wikipedia.org/wiki/Biserica_evanghelic%C4%83_din_Cluj-Napoca");

     //   databaseReference = FirebaseDatabase.getInstance().getReference("exchangePoints");

      //  String id = databaseReference.push().getKey();
       // ep.setId(id);

//        String id1 = databaseReference.push().getKey();
//        ep1.setId(id1);
//
//        String id2 = databaseReference.push().getKey();
//        ep2.setId(id2);
//
//        String id3 = databaseReference.push().getKey();
//        ep3.setId(id3);
//
//        String id4 = databaseReference.push().getKey();
//        ep4.setId(id4);

//        String id5 = databaseReference.push().getKey();
//        ep5.setId(id5);

//        String id6 = databaseReference.push().getKey();
//        ep6.setId(id6);

      //  databaseReference.child(ep.getId()).setValue(ep);
//        databaseReference.child(ep1.getId()).setValue(ep1);
//        databaseReference.child(ep2.getId()).setValue(ep2);
//        databaseReference.child(ep3.getId()).setValue(ep3);
//        databaseReference.child(ep4.getId()).setValue(ep4);
       // databaseReference.child(ep5.getId()).setValue(ep5);
   //     databaseReference.child(ep6.getId()).setValue(ep6);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_points);
        //writeNewExchange();
        userOptionsDialog = new Dialog(this);
        logOutDialog = new Dialog(this);
        mapViewButton = findViewById(R.id.mapViewButton);
        userProfileButton = findViewById(R.id.userProfileButton);

        recyclerView = findViewById(R.id.exchange_rv);
        adapter = new ExchangeRecyclerViewAdapter();

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        retrieveAllExchanges();

       // showExchangesifPoints();

        userProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserOptionMenu();
            }
        });

        mapViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ExchangePoints.this, MapsActivity.class));
            }
        });
}

    /**
     * method retrieves all exchanges
     * from db
     */
    private void retrieveAllExchanges(){
        databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference db = databaseReference.child("exchangePoints");

        final ImageView image = findViewById(R.id.exchange_imageView);
        final Button b=findViewById(R.id.use_points);
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String email=currentUser.getEmail();

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allExchanges = new ArrayList<>();
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    Exchange ex = new Exchange();
                    ex.setTitle(ds.child("title").getValue().toString());
                    ex.setDescription(ds.child("description").getValue().toString());
                    ex.setPoints(Integer.valueOf(ds.child("points").getValue().toString()));
                    ex.setQrCode(ds.child("qrCode").getValue().toString());

                    allExchanges.add(ex);
                }
                adapter.setExchanges(allExchanges);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        db.addListenerForSingleValueEvent(eventListener);
    }



    /**
     * method displays menu for user profile options
     */
    public void showUserOptionMenu(){
        TextView userProfileView;
        TextView userPicturesView;
        TextView userCommentsView;
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
        userPicturesView = userOptionsDialog.findViewById(R.id.userPicturesBtn);
        logOutView = userOptionsDialog.findViewById(R.id.logoutBtn);


        userCommentsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ExchangePoints.this, UserComments.class));
            }
        });

        userProfileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ExchangePoints.this, UserProfile.class));
            }
        });

        userPicturesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ExchangePoints.this, UserPictures.class));
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
                Intent intent = new Intent(ExchangePoints.this, MainActivity.class);
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
