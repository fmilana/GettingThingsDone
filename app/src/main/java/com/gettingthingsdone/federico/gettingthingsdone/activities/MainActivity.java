package com.gettingthingsdone.federico.gettingthingsdone.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gettingthingsdone.federico.gettingthingsdone.Item;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.CalendarFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.InTrayFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.MaybeLaterFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.ProjectsFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.ReferenceFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.TrashFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.WaitingForFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;
    private ProgressBar progressBar;

    public static DatabaseReference databaseReference;
    public static FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        getSupportActionBar().hide();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        //if user is already logged in
        if (firebaseAuth.getCurrentUser() != null) {
            Intent intent = new Intent(MainActivity.this, MainFragmentActivity.class);
            MainActivity.this.startActivity(intent);
            finish();
        }

        emailEditText = (EditText) findViewById(R.id.insert_email_address);
        passwordEditText = (EditText) findViewById(R.id.insert_password);
        loginButton = (Button) findViewById(R.id.log_in_button);
        registerButton = (Button) findViewById(R.id.register_button);
        progressBar = (ProgressBar) findViewById(R.id.log_in_progress_bar);


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn(emailEditText.getText().toString().trim(), passwordEditText.getText().toString().trim());
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

    }

    private void signIn(String email, String password) {

        if (TextUtils.isEmpty(email)) {
            //email is empty
            Toast.makeText(MainActivity.this, "Please insert your email address",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isEmailValid(email)) {
            Toast.makeText(MainActivity.this, "Please enter a valid email address",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            //password is empty
            Toast.makeText(MainActivity.this, "Please insert your password",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //user is successfully logged in
                            progressBar.setVisibility(View.INVISIBLE);

                            Intent intent = new Intent(MainActivity.this, MainFragmentActivity.class);
                            MainActivity.this.startActivity(intent);
                            finish();
                        } else {
                            progressBar.setVisibility(View.INVISIBLE);

                            Toast.makeText(MainActivity.this, "Authentication failed",
                                    Toast.LENGTH_SHORT).show();
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
