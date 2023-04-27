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

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        // progressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);





        //Handle click, begin register

        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
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
    private void validateData() {

        //xac thuc du lieu truoc khi tao tai khoan

        //get data
        name = binding.nameEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();
        String cPassword = binding.confirmPasswordEt.getText().toString().trim();

        if (TextUtils.isEmpty(name)){
            Toast.makeText(this,"Enter your name...",Toast.LENGTH_LONG).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            Toast.makeText(this,"Invalid email pattern...",Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this,"Enter password...",Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(cPassword)) {
            Toast.makeText(this,"Confirm password...",Toast.LENGTH_LONG).show();
        } else if (!password.equals(cPassword)) {
            Toast.makeText(this,"Password don't match...!",Toast.LENGTH_LONG).show();
        }else {
            CreateUserAccount();
        }
    }

    private void CreateUserAccount() {
        //show progress
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        //create user in firebase
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                //account creation success,
                updateUserInfor();//add in realtime database


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure( Exception e) {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();

            }
        });
    }

    private void updateUserInfor() {
        progressDialog.setMessage("Saving user info...");

        //timestamp
        long timestamp = System.currentTimeMillis();

        //get current user uid, since user is registered
        String uid = firebaseAuth.getUid();

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("uid",uid);
        hashMap.put("name",name);
        hashMap.put("email",email);
        hashMap.put("ProfireImage",""); //add empty , will do later
        hashMap.put("userType","user"); // set mac dinh la user, admin sua o firebase
        hashMap.put("timestamp",timestamp);

        // set data to database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        //data added to db
                        Toast.makeText(RegisterActivity.this,"Account created...",Toast.LENGTH_LONG).show();
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

    @Override
    protected void onResume() {
        super.onResume();

        // Nếu người dùng đã đăng nhập
        if (currentUser != null) {
            // Cập nhật thời gian gần nhất mà người dùng đã hoạt động trên ứng dụng của bạn
            usersRef.child(currentUser.getUid()).child("lastActiveTimestamp").setValue(System.currentTimeMillis());
        }
    }
}