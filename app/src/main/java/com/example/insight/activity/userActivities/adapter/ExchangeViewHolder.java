package com.example.insight.activity.userActivities.adapter;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.insight.R;
import com.example.insight.activity.userActivities.ExchangePoints;
import com.example.insight.entity.Exchange;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.HashMap;
import java.util.Map;

public class ExchangeViewHolder extends RecyclerView.ViewHolder {
    private ImageView image;
    private TextView tv2;
    private Button button;
    private DatabaseReference databaseReference;

    public ExchangeViewHolder(View view){
        super(view);

        image = view.findViewById(R.id.exchange_imageView);
        tv2=view.findViewById(R.id.textViewDescr);
        button=view.findViewById(R.id.use_points);
    }

    private void showExchangesifPoints(final Exchange exchange) {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String email = currentUser.getEmail();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("users");

        databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {

                    final Integer points = Integer.valueOf(childDataSnapshot.child("points").getValue().toString());

                    if (exchange.getPoints() <= points) {
                        button.setEnabled(true);
                    } else {
                        button.setEnabled(false);
                    }
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String key = childDataSnapshot.getKey();
                            databaseReference = databaseReference.child(key);
                            Map<String, Object> map = new HashMap<>();
                            map.put("points", points - exchange.getPoints());
                            databaseReference.updateChildren(map);
                            try {
                                image.setImageBitmap(generateQrCode(exchange));
                            } catch (WriterException e) {
                                e.printStackTrace();
                            }
                            button.setVisibility(View.GONE);
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public Bitmap generateQrCode(Exchange exchange) throws WriterException {
        try {
            BitMatrix result = new QRCodeWriter().encode(exchange.getQrCode(), BarcodeFormat.QR_CODE, 412, 412);
            Bitmap    bitmap = Bitmap.createBitmap(result.getWidth(), result.getHeight(), Bitmap.Config.ARGB_8888);

            for (int y = 0; y < result.getHeight(); y++) {
                for (int x = 0; x < result.getWidth(); x++) {
                    if (result.get(x, y)) {
                        bitmap.setPixel(x, y, Color.BLACK);
                    }
                }
            }
            return bitmap;
        } catch (WriterException e) {
            return Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
        }
    }

    public void bind(Exchange exchange) throws WriterException {
        showExchangesifPoints(exchange);
        tv2.setText(exchange.getTitle()+"\n\n"+exchange.getDescription()+"\n\nPoints needed: "+exchange.getPoints());
        button.setText("Exchange");
    }
}
