package com.example.instagram.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.instagram.R;
import com.example.instagram.models.GlideApp;
import com.example.instagram.models.User;
import com.parse.ParseQuery;

import java.util.Stack;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AuthenticateActivity extends AppCompatActivity {
    // Holds layout ids of each view we have navigated through
    private Stack<Integer> mNavigationStack;
    private int mCurrentView;

    @Nullable @BindView(R.id.tv_username) TextView tvUsername;
    @Nullable @BindView(R.id.tv_username_unavailable) TextView tvUsernameUnavailable;
    @Nullable @BindView(R.id.et_username) EditText etUsername;
    @Nullable @BindView(R.id.et_password) EditText etPassword;
    @Nullable @BindView(R.id.iv_outline) ImageView ivProfile;
    @Nullable @BindView(R.id.iv_border) ImageView ivBorder;
    @Nullable @BindView(R.id.btn_create_account) Button btnCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bypass authentication activity if user is already logged in
        if(User.getCurrentUser() != null) {
            switchToHomeActivity(null);
        }

        // Set the current view
        setContentView(mCurrentView = R.layout.activity_authenticate_home);

        // Initialize the navigation stack
        mNavigationStack = new Stack<>();
    }

    /**
     * Override the back button to use the navigation stack to decide which page to return to
     */
    @Override
    public void onBackPressed() {
        // If the navigation stack is empty then do the default behavior
        if(mNavigationStack.empty()) {
            super.onBackPressed();
        } else {
            // Get the previous view from the navigation stack
            setContentView(mCurrentView = mNavigationStack.pop());
        }
    }

    public void switchToLoginView(View view) {
        mNavigationStack.push(mCurrentView);
        setContentView(mCurrentView = R.layout.activity_authenticate_login);
        ButterKnife.bind(this);
    }

    public void switchToCreateView(View view) {
        mNavigationStack.push(mCurrentView);
        setContentView(mCurrentView = R.layout.activity_authenticate_create);
        ButterKnife.bind(this);


        etUsername.addTextChangedListener(new TextWatcher() {
            ParseQuery query = null;

            @Override
            public void afterTextChanged(Editable s) {
                // Cancel the old query if its still running
                if(query != null && query.isRunning()) query.cancel();

                // Set the edit text color back to normal
                etUsername.setBackground(getResources().getDrawable(R.drawable.et_default));

                // Check if the username is available
                query = User.isUsernameAvailable(s.toString(), isAvailable -> {
                    if(isAvailable) {
                        // Update views to reflect a valid username
                        tvUsernameUnavailable.setVisibility(View.GONE);
                        if(!s.toString().isEmpty()) {
                            etUsername.setBackground(getResources().getDrawable(R.drawable.et_green));

                            // Check if the password is also valid before enabling the button
                            if(!etPassword.getText().toString().isEmpty()) {
                                btnCreateAccount.setEnabled(true);
                            }
                        }
                    } else {
                        // Update views to reflect an invalid username
                        etUsername.setBackground(getResources().getDrawable(R.drawable.et_red));
                        tvUsernameUnavailable.setText(
                                String.format("The username %s is not available", s.toString()));
                        tvUsernameUnavailable.setVisibility(View.VISIBLE);
                        btnCreateAccount.setEnabled(false);
                    }
                });
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // Check if the username and password are valid and enable button accordingly
                if(!s.toString().isEmpty() && tvUsernameUnavailable.getVisibility() == View.GONE) {
                    btnCreateAccount.setEnabled(true);
                } else {
                    btnCreateAccount.setEnabled(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    public void switchToWelcomeView(User user) {
        mNavigationStack.push(mCurrentView);
        setContentView(mCurrentView = R.layout.activity_authenticate_welcome);
        ButterKnife.bind(this);

        // Setup the profile image and border
        GlideApp.with(this)
                .load(R.drawable.background)
                .transform(new CircleCrop())
                .into(ivBorder);
        GlideApp.with(this)
//                .load(user.getString("profile_image"))  TODO: Load profile image here
                .load(R.drawable.avatar)
                .transform(new CircleCrop())
                .error(R.drawable.avatar)
                .placeholder(R.drawable.avatar)
                .into(ivProfile);

        tvUsername.setText(user.getUsername());
    }

    public void switchToHomeActivity(View view) {
        Toast.makeText(this, "Logged in user successfully", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    public void onLoginClick(View view) {
        // Get username and password from views
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        // Log in user
        User.logInInBackground(username, password, (user, e) -> {
            if(e == null) {
                switchToHomeActivity(null);
            } else {
                Log.e("AuthenticateActivity", "Login failure", e);
                Toast.makeText(this, "Username/password were incorrect", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onCreateClick(View view) {
        // Get username and password from views
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        // Create the user and set its properties
        final User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        // Sign up the user
        user.signUpInBackground(e -> {
            if (e == null) {
                switchToWelcomeView(user);
            } else {
                Log.e("AuthenticateActivity", "User creation failure", e);
                Toast.makeText(this, "Could not create account", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
