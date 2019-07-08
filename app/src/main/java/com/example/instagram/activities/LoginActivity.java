package com.example.instagram.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.instagram.R;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login("test", "password");
    }

    private void login(String username, String password) {
        ParseUser.logInInBackground(username, password, (ParseUser user, ParseException e) -> {
           if(e == null) {
               Toast.makeText(this, "Logged in user", Toast.LENGTH_SHORT).show();

               Intent i = new Intent(this, HomeActivity.class);
               startActivity(i);
               finish();
           } else {
               Log.e("LoginActivity", "Login failure", e);
               Toast.makeText(this, "Login failure", Toast.LENGTH_SHORT).show();
           }
        });
    }
}
