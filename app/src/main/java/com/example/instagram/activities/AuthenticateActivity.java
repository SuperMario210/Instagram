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
import android.widget.ViewAnimator;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.instagram.R;
import com.example.instagram.models.GlideApp;
import com.example.instagram.models.User;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Stack;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AuthenticateActivity extends AppCompatActivity {
    // Indices for each view inside the view animator
    private static final int HOME = 0;
    private static final int LOGIN = 1;
    private static final int CREATE = 2;
    private static final int WELCOME = 3;

    // Holds layout ids of each view we have navigated through
    private Stack<Integer> mNavigationStack;
    private int mCurrentView;

    ViewAnimator viewAnimator;
    @BindView(R.id.tv_username) TextView tvUsername;
    @BindView(R.id.tv_username_unavailable) TextView tvUsernameUnavailable;
    @BindView(R.id.et_username_create) EditText etUsernameCreate;
    @BindView(R.id.et_password_create) EditText etPasswordCreate;
    @BindView(R.id.et_username) EditText etUsername;
    @BindView(R.id.et_password) EditText etPassword;
    @BindView(R.id.iv_outline) ImageView ivProfile;
    @BindView(R.id.iv_border) ImageView ivBorder;
    @BindView(R.id.btn_create_account) Button btnCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bypass authentication activity if user is already logged in
        if(User.getCurrentUser() != null) {
            switchToHomeActivity(null);
        }

        // Set the current view
        setContentView(R.layout.activity_authenticate);

        initViewAnimator();

        // Now that all views have been inflated, use butterknife to bind the rest of the views
        ButterKnife.bind(this);

        // Initialize the navigation stack
        mNavigationStack = new Stack<>();
    }

    /**
     * Adds all views to the view animator and sets up the animations
     */
    private void initViewAnimator() {
        // Add views to the view animator
        viewAnimator = findViewById(R.id.view_animator);
        viewAnimator.addView(getLayoutInflater()
                .inflate(R.layout.activity_authenticate_home, viewAnimator, false), HOME);
        viewAnimator.addView(getLayoutInflater()
                .inflate(R.layout.activity_authenticate_login, viewAnimator, false), LOGIN);
        viewAnimator.addView(getLayoutInflater()
                .inflate(R.layout.activity_authenticate_create, viewAnimator, false), CREATE);
        viewAnimator.addView(getLayoutInflater()
                .inflate(R.layout.activity_authenticate_welcome, viewAnimator, false), WELCOME);

        // Set up view animator animations
        viewAnimator.setAnimateFirstView(false);
        viewAnimator.setInAnimation(this, R.anim.fade_in);
        viewAnimator.setOutAnimation(this, R.anim.fade_out);
    }

    /**
     * Override the back button to use the navigation stack to decide which page to return to
     */
    @Override
    public void onBackPressed() {
        if(mNavigationStack.empty()) {
            // If the navigation stack is empty then do the default behavior
            super.onBackPressed();
        } else {
            // Otherwise get the previous view from the navigation stack
            viewAnimator.setDisplayedChild(mCurrentView = mNavigationStack.pop());
        }
    }

    public void switchToLoginView(View view) {
        mNavigationStack.push(mCurrentView);
        viewAnimator.setDisplayedChild(mCurrentView = LOGIN);
    }

    public void switchToCreateView(View view) {
        mNavigationStack.push(mCurrentView);
        viewAnimator.setDisplayedChild(mCurrentView = CREATE);

        etUsernameCreate.addTextChangedListener(new TextWatcher() {
            ParseQuery query = null;

            @Override
            public void afterTextChanged(Editable s) {
                // Cancel the old query if its still running
                if(query != null && query.isRunning()) query.cancel();

                // Set the edit text color back to normal
                etUsernameCreate.setBackground(getResources().getDrawable(R.drawable.et_default));

                // Check if the username is available
                query = User.isUsernameAvailable(s.toString(), isAvailable -> {
                    if(isAvailable) {
                        // Update views to reflect a valid username
                        tvUsernameUnavailable.setVisibility(View.GONE);
                        if(!s.toString().isEmpty()) {
                            etUsernameCreate.setBackground(getResources().getDrawable(R.drawable.et_green));

                            // Check if the password is also valid before enabling the button
                            if(!etPassword.getText().toString().isEmpty()) {
                                btnCreateAccount.setEnabled(true);
                            }
                        }
                    } else {
                        // Update views to reflect an invalid username
                        etUsernameCreate.setBackground(getResources().getDrawable(R.drawable.et_red));
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

        etPasswordCreate.addTextChangedListener(new TextWatcher() {
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
        viewAnimator.setDisplayedChild(mCurrentView = WELCOME);

        // Setup the profile image and border
        GlideApp.with(this)
                .load(R.drawable.background)
                .transform(new CircleCrop())
                .into(ivBorder);
        GlideApp.with(this)
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
        ParseUser.logInInBackground(username, password, (user, e) -> {
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
        String username = etUsernameCreate.getText().toString();
        String password = etPasswordCreate.getText().toString();

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
