package com.example.instagram.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.instagram.R;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.Stack;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AuthenticateActivity extends AppCompatActivity {
    // Holds layout ids of each view we have navigated through
    private Stack<Integer> mNavigationStack;

    @BindView(R.id.et_username) EditText etUsername;
    @BindView(R.id.et_password) EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bypass authentication activity if user is already logged in
        if(ParseUser.getCurrentUser() != null) {
            switchToHomeActivity();
        }

        setContentView(R.layout.activity_authenticate_home);

        // Initialize the navigation stack
        mNavigationStack = new Stack<>();
    }

    @Override
    public void onBackPressed() {
        // If the navigation stack is empty then do the default behavior
        if(mNavigationStack.empty()) {
            super.onBackPressed();
        } else {
            // Get the previous view from the navigation stack
            setContentView(mNavigationStack.pop());
        }
    }

    public void switchToLoginView(View view) {
        mNavigationStack.push(R.layout.activity_authenticate_home);
        setContentView(R.layout.activity_authenticate_login);
        ButterKnife.bind(this);
    }

    public void switchToCreateView(View view) {
        mNavigationStack.push(R.layout.activity_authenticate_home);
        setContentView(R.layout.activity_authenticate_login);
        ButterKnife.bind(this);
    }

    private void switchToHomeActivity() {
        Toast.makeText(this, "Logged in user successfully", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, HomeActivity.class);
        startActivity(i);
        finish();
    }

    public void onLoginClick(View view) {
        // Get username and password from views
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        // Log in user
        ParseUser.logInInBackground(username, password, (ParseUser user, ParseException e) -> {
            if(e == null) {
                switchToHomeActivity();
            } else {
                Log.e("AuthenticateActivity", "Login failure", e);
                Toast.makeText(this, "Username/password were incorrect", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
