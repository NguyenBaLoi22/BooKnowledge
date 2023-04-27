package com.example.booknowledge_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import com.example.booknowledge_app.databinding.ActivityDashboardUserBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardUserActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private ActivityDashboardUserBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //init firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        //handle logout
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                checkUser();
            }
        });
    }
    private void checkUser() {
        //get current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser==null){
            //not logged in, goto main screen
            startActivity(new Intent(DashboardUserActivity.this,MainActivity.class));
            finish();
        }else {
            //logged in, get user info
            String email = firebaseUser.getEmail();
            //set in textview toolbar
            binding.title.setText(email);


        }
    }

}
