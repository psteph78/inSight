package com.example.insight.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.insight.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;
    private Button signupButton;

    FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signUpButton);

        firebaseAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(emailInput.getText().toString().isEmpty() || passwordInput.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "You didn't fill in all fields!", Toast.LENGTH_LONG).show();
                }
                else {
                    firebaseAuth.signInWithEmailAndPassword(emailInput.getText().toString(), passwordInput.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                if (Objects.requireNonNull(firebaseAuth.getCurrentUser().isEmailVerified())){
                                    startActivity(new Intent(MainActivity.this, MapViewActivity.class));
                                }
                                else{
                                    Toast.makeText(MainActivity.this, "Please verify your email address!", Toast.LENGTH_LONG).show();
                                }
                            }
                            else{
                                Toast.makeText(MainActivity.this, "Wrong credentials!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SignUpActivity.class));
            }
        });
    }
}
