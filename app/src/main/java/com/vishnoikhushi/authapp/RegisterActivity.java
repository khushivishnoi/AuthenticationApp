package com.vishnoikhushi.authapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class RegisterActivity extends AppCompatActivity {
    private String TAG = "RegisterActivity";
    private Button rgst_btn;
    private ProgressBar progressBar;
    private TextView login;

    private EditText mEmail;
    private EditText mName;
    private EditText mPassword;
    private EditText mRepassword;

    private FirebaseAuth auth;
    private FirebaseFirestore fstore;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        mEmail = findViewById(R.id.email);
        mName = findViewById(R.id.fullName);
        mPassword = findViewById(R.id.password);
        mRepassword = findViewById(R.id.repassword);

        rgst_btn = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        login = findViewById(R.id.btn_log);
        rgst_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String repassword = mRepassword.getText().toString().trim();
                final String name = mName.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    mName.setError("Name is Required");
                }
                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Email is Required.");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Password is Required.");
                    return;
                }

                if (password.length() < 6) {
                    mPassword.setError("Password Must be >= 6 Characters");
                    return;
                }
                if (password==(repassword)) {
                    mRepassword.setError("Password not same");
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                registeredUser(name, email, password);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });
    }

    /*
    *This method is used to store user detail in firestore
     */
    private void registeredUser(String name, String email, String password) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
                RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Successfully Registered",
                                    Toast.LENGTH_SHORT).show();
                            userID = auth.getCurrentUser().getUid();
                            User user = new User();
                            user.setUserID(userID);
                            user.setName(name);
                            user.setEmail(email);
                            user.setPassword(password);
                            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                                    .build();
                            fstore.setFirestoreSettings(settings);
                            DocumentReference newUserRef = fstore
                                    .collection("users")
                                    .document(FirebaseAuth.getInstance().getUid());
                            newUserRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d(TAG, "User profile is created for " + userID);
                                }
                            });

                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();

                        } else {
                            Toast.makeText(RegisterActivity.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }
}