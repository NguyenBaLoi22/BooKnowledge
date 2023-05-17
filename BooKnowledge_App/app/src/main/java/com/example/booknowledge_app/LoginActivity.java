package com.example.booknowledge_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.booknowledge_app.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;
    //firebase auth
    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;
    private DatabaseReference usersRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Hãy Chờ");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.noAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CheckDataInput();
            }
        });

        binding.forgotTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,ForgotPasswordActivity.class));

            }
        });
    }
    private String email = "", password = "";
    private void CheckDataInput() {
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();
        //validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this,"Định dạng email không hợp lệ",Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this,"Nhập mật khẩu",Toast.LENGTH_LONG).show();
        }
        else {
            login();
        }
    }
    private void login() {
        progressDialog.setMessage("Đang Đăng Nhập");
        progressDialog.show();

        //login user firebase
        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //login success,check if user or admin
                        checkType();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //login fail
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();

                    }
                });
    }
    private void checkType() {
        progressDialog.setMessage("Kiểm tra người dùng!");
        //check user or admin
        //get current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        //Check in database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        if (currentUser != null) {
            // Cập nhật thời gian gần nhất mà người dùng đã hoạt động trên ứng dụng của bạn
            usersRef.child(currentUser.getUid()).child("lastActiveTimestamp").setValue(System.currentTimeMillis());
        }
        ref.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                progressDialog.dismiss();
                //get user type
                String userType = ""+snapshot.child("userType").getValue();
                //check user type
                if(userType.equals("user")){
                    // la user thi chuyen qua giao dien nguoi dung
                    startActivity(new Intent(LoginActivity.this, DashboardUserActivity.class));
                    finish();
                }
                else if (userType.equals("admin")){
                    // la admin thi chuyen qua giao dien admin
                    startActivity(new Intent(LoginActivity.this,DashboardAdminActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

}
