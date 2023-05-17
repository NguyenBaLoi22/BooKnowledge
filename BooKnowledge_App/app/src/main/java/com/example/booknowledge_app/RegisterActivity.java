package com.example.booknowledge_app;


import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import com.example.booknowledge_app.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
     ActivityRegisterBinding binding;
     FirebaseAuth firebaseAuth;
     ProgressDialog progressDialog;
    private DatabaseReference usersRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //khai báo firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        // progressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);

        //set sự kiện cho các button
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDataInput();
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    private String name="";
    private String email="";
    private String password ="";
    private void checkDataInput() {
        //xac thuc du lieu truoc khi tao tai khoan
        //get data
        name = binding.nameEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();
        String cPassword = binding.confirmPasswordEt.getText().toString().trim();
        if (TextUtils.isEmpty(name)){
            Toast.makeText(this,"Nhập tên của bạn...",Toast.LENGTH_LONG).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this,"Sai định dạng email...",Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this,"Nhập Mật khẩu...",Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(cPassword)) {
            Toast.makeText(this,"Nhập Xác Nhận Mật khẩu...",Toast.LENGTH_LONG).show();
        } else if (!password.equals(cPassword)) {
            Toast.makeText(this,"Xác Nhận Mật Khẩu Sai...!",Toast.LENGTH_LONG).show();
        }else {
            CreateAccount();
        }
    }

    private void CreateAccount() {
        //show progress
        progressDialog.setMessage("Đang Tạo Tài Khoản...");
        progressDialog.show();
        //create account
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                // tao thanh cong
                updateUserInfo();//them du lieu vao realtime database
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure( Exception e) {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this,"Tạo tài khoản thất bại vì"+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateUserInfo() {
        progressDialog.setMessage("Lưu thông tin người dùng...");

        //timestamp
        long timestamp = System.currentTimeMillis();
        //id = thoi gian tao tai khoan
        //get current user uid, since user is registered
        String uid = firebaseAuth.getUid();
        String lastTimeActive = ""+ System.currentTimeMillis();
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("uid",uid);
        hashMap.put("name",name);
        hashMap.put("email",email);
        hashMap.put("ProfileImage",""); //add empty , will do later
        hashMap.put("userType","user"); // set mac dinh la user, admin sua o firebase
        hashMap.put("timestamp",timestamp);
        hashMap.put("lastActiveTimestamp",lastTimeActive);

        // set data to database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        //data added to db
                        Toast.makeText(RegisterActivity.this,"Tạo Tài khoản thành công..",Toast.LENGTH_LONG).show();
                        startActivity(new Intent(RegisterActivity.this, DashboardUserActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure( Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();

                    }
                });

    }

}