package com.gettingthingsdone.federico.gettingthingsdone.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        setContentView(R.layout.splash_activity);

        getSupportActionBar().hide();

        signInCheck();
    }

    @Override
    protected void onResume() {
        super.onResume();

        signInCheck();
    }

    private void signInCheck() {
        databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //if user is already logged in
                if (firebaseAuth.getCurrentUser() != null && dataSnapshot.hasChild(firebaseAuth.getCurrentUser().getUid())) {
                    Intent intent = new Intent(SplashActivity.this, MainFragmentActivity.class);
                    SplashActivity.this.startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(SplashActivity.this, LogInActivity.class);
                    SplashActivity.this.startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Intent intent = new Intent(SplashActivity.this, LogInActivity.class);
                SplashActivity.this.startActivity(intent);
                finish();
            }
        });
    }
}
