package com.example.insight.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.insight.R;
import com.example.insight.entity.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpActivity extends AppCompatActivity {

    private EditText username;
    private EditText email;
    private EditText password;
    private EditText confirmPassword;
    private Toolbar actionbar;

    private Button signupButton;
    private Button backButton;

    private CircleImageView circleImageView;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private Uri selectedImageUri;

    private StorageReference storageRef;

    private static final int RESULT_LOAD_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        actionbar = findViewById(R.id.app_bar);
        setSupportActionBar(actionbar);

        username = findViewById(R.id.usernameField);
        email = findViewById(R.id.emailField);
        password = findViewById(R.id.passwordField);
        confirmPassword = findViewById(R.id.confirmPasswordField);
        signupButton = findViewById(R.id.signUpButton);
        backButton = findViewById(R.id.backButton);
        circleImageView = findViewById(R.id.profile_image);

        storageRef = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(username.getText().toString().isEmpty() || email.getText().toString().isEmpty() || password.getText().toString().isEmpty() ||
                        confirmPassword.getText().toString().isEmpty()){
                    Toast.makeText(SignUpActivity.this, "All fields are required!", Toast.LENGTH_LONG).show();
                } else if (!username.getText().toString().matches("^[a-zA-Z0-9]{3,}$")){
                    Toast.makeText(SignUpActivity.this, "Username must contain a minimum 3 characters. Only letters and digits allowed. Can't contain empty spaces.", Toast.LENGTH_LONG).show();
                } else if (username.getText().toString().length() > 20){
                    Toast.makeText(SignUpActivity.this, "Username is too long!", Toast.LENGTH_LONG).show();
                } else if (!password.getText().toString().equals(confirmPassword.getText().toString())){
                    Toast.makeText(SignUpActivity.this, "Password does not match!", Toast.LENGTH_LONG).show();
                //} else if (!password.getText().toString().matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")){
                //    Toast.makeText(SignUpActivity.this, "Password is too weak! It must contain a minimum of 8 characters, at least one letter, one digit and one special character!", Toast.LENGTH_LONG).show();
                }

                else{
                    firebaseAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    if (selectedImageUri != null){
                                        addUserWithProfilePic();
                                    } else {
                                        addUser();
                                    }

                                    Objects.requireNonNull(firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(SignUpActivity.this, "Registered successfully. Verify your email.", Toast.LENGTH_LONG).show();
                                                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                            }
                                            else{
                                                Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }));
                                }
                                else{
                                    Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
            }
        });

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 100);
            }
        });
    }
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (resultCode == RESULT_OK) {
                    // Get the url from data
                    //final Uri selectedImageUri = data.getData();
                    selectedImageUri = data.getData();
                    if (null != selectedImageUri) {
                        // Get the path from the Uri
                        String path = getPathFromURI(selectedImageUri);
                        // Set the image in ImageView
                        circleImageView.post(new Runnable() {
                            @Override
                            public void run() {
                                InputStream imageStream = null;
                                try {
                                    imageStream = getContentResolver().openInputStream(
                                            selectedImageUri);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }

                                Bitmap bmp = BitmapFactory.decodeStream(imageStream);

                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                bmp.compress(Bitmap.CompressFormat.PNG, RESULT_LOAD_IMAGE, stream);
                                byte[] byteArray = stream.toByteArray();
                                try {
                                    stream.close();
                                    stream = null;
                                } catch (IOException e) {

                                    e.printStackTrace();
                                }

                                circleImageView.setImageURI(selectedImageUri);

                            }
                        });

                    }
                }
            }
        }).start();
    }


    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }


    private String getFileExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void addUserWithProfilePic(){
        if(selectedImageUri != null);
        final StorageReference fileReference = storageRef.child(System.currentTimeMillis() + "." + getFileExtension(selectedImageUri));
        fileReference.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        User user = new User();
                        user.setUsername(username.getText().toString());
                        user.setEmail(email.getText().toString());
                        user.setPoints(0);
                        user.setProfileImgName(getPathFromURI(selectedImageUri).trim());
                        user.setProfileImgUrl(uri.toString());

                        String id = databaseReference.push().getKey();
                        user.setId(id);

                        databaseReference.child(user.getId()).setValue(user);
                    }
                });
            }
        });
    }

    private void addUser(){
        User user = new User();
        user.setUsername(username.getText().toString());
        user.setEmail(email.getText().toString());
        user.setPoints(0);
        user.setProfileImgName("");
        user.setProfileImgUrl("");

        String id = databaseReference.push().getKey();
        user.setId(id);

        databaseReference.child(user.getId()).setValue(user);
    }
}
