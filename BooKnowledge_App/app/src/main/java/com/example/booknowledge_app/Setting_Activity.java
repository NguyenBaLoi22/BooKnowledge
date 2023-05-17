package com.example.booknowledge_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.booknowledge_app.databinding.ActivitySettingBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Setting_Activity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private ActivitySettingBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseAuth = FirebaseAuth.getInstance();

        binding.nav.setSelectedItemId(R.id.menu_setting);
        binding.nav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_profile:
                        if (currentUser != null) {
                            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                            overridePendingTransition(0,0);
                        }else{
                            Toast.makeText(Setting_Activity.this, "Bạn cần đăng nhập để sử dụng chức năng này!!!", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    case R.id.menu_home:
                        startActivity(new Intent(getApplicationContext(), DashboardUserActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.menu_setting:
                        return true;
                }
                return false;
            }
        });

        binding.settingViewReadTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Setting_Activity.this, "Chức Năng Hiện Chưa Hoàn Thiện!!!", Toast.LENGTH_SHORT).show();
            }
        });
        binding.settingThemeTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Setting_Activity.this, "Chức Năng Hiện Chưa Hoàn Thiện!!!", Toast.LENGTH_SHORT).show();
            }
        });
        binding.notificationTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Setting_Activity.this, "Chức Năng Hiện Chưa Hoàn Thiện!!!", Toast.LENGTH_SHORT).show();
            }
        });
        binding.languageTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Setting_Activity.this, "Chức Năng Hiện Chưa Hoàn Thiện!!!", Toast.LENGTH_SHORT).show();
            }
        });

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
            startActivity(new Intent(Setting_Activity.this,MainActivity.class));
            finish();
        }
    }
}
