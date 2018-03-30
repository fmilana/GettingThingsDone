package com.gettingthingsdone.federico.gettingthingsdone.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

/**
 * Created by feder on 22-Mar-18.
 */

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText emailEditText;
    private Button sendEmailButton;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

//        getSupportActionBar().hide();
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        firebaseAuth = LogInActivity.firebaseAuth;
        databaseReference = LogInActivity.databaseReference;

        emailEditText = (EditText) findViewById(R.id.password_reset_email_address_edit_text);
        sendEmailButton = (Button) findViewById(R.id.password_reset_button);
        progressBar = (ProgressBar) findViewById(R.id.password_reset_progress_bar);

        sendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                sendPasswordResetEmail();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return true;
    }

    private void sendPasswordResetEmail() {
        String email = emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, R.string.please_insert_an_email_address, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!RegisterActivity.isEmailValid(email)) {
            Toast.makeText(this, R.string.please_insert_a_valid_email_address, Toast.LENGTH_SHORT).show();
            return;
        }

        final ArrayList<Boolean> invalid = new ArrayList<>();
        invalid.add(false);

        firebaseAuth.fetchProvidersForEmail(email).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                if (task.getResult().getProviders().isEmpty()) {
                    Toast.makeText(ForgotPasswordActivity.this, R.string.email_address_does_not_belong_to_any_account, Toast.LENGTH_SHORT).show();
                    invalid.set(0, true);
                }
            }
        });

        if (invalid.get(0)) {
            return;
        }

        
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPasswordActivity.this, R.string.email_sent, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        progressBar.setVisibility(View.GONE);
    }
}
