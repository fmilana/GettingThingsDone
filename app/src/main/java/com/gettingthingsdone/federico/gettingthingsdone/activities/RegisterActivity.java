package com.gettingthingsdone.federico.gettingthingsdone.activities;

import android.content.Intent;
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
import com.gettingthingsdone.federico.gettingthingsdone.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Federico on 06-Feb-18.
 */

public class RegisterActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button registerButton;

    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        firebaseAuth = MainActivity.firebaseAuth;
        databaseReference = MainActivity.databaseReference;

        emailEditText = (EditText) findViewById(R.id.register_email_address);
        passwordEditText = (EditText) findViewById(R.id.register_password);
        confirmPasswordEditText = (EditText) findViewById(R.id.confirm_password);
        registerButton = (Button) findViewById(R.id.register_button);
        progressBar = (ProgressBar) findViewById(R.id.register_progress_bar);


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
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

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmedPassword = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            //email is empty
            Toast.makeText(RegisterActivity.this, R.string.please_insert_an_email_address,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isEmailValid(email)) {
            Toast.makeText(RegisterActivity.this, R.string.please_insert_a_valid_email_address,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            //password is empty
            Toast.makeText(RegisterActivity.this, R.string.please_insert_a_password,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(confirmedPassword)) {
            //confirmed password is empty
            Toast.makeText(RegisterActivity.this, R.string.please_confirm_your_password,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmedPassword)) {
            //passwords don't match
            Toast.makeText(RegisterActivity.this, R.string.passwords_do_not_match,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //user is successfully registered
                            progressBar.setVisibility(View.INVISIBLE);

                            Toast.makeText(RegisterActivity.this, R.string.registration_successful,
                                    Toast.LENGTH_SHORT).show();


                            /////////////////////////add new user to firebase///////////////////////////////
                            User newUser = new User(emailEditText.getText().toString().trim());
                            databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).setValue(newUser);

                            Intent intent = new Intent(RegisterActivity.this, MainFragmentActivity.class);
                            RegisterActivity.this.startActivity(intent);
                        } else {
                            progressBar.setVisibility(View.INVISIBLE);

                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                Toast.makeText(RegisterActivity.this, R.string.please_enter_a_password_longer_than_6_characters, Toast.LENGTH_SHORT).show();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                Toast.makeText(RegisterActivity.this, R.string.please_insert_a_valid_email_address, Toast.LENGTH_SHORT).show();
                            } catch (FirebaseAuthUserCollisionException e) {
                                Toast.makeText(RegisterActivity.this, R.string.an_account_already_exists_with_that_email_address, Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(RegisterActivity.this, R.string.something_went_wrong_please_try_again, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
