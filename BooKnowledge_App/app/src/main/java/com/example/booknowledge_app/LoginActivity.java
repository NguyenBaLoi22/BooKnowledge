package com.example.booknowledge_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
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
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);



        //handle register screen
        binding.noAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });

        //handle login
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }
    private String email = "", password = "";
    private void validateData() {
        //before login , validateData

        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();

        //validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            Toast.makeText(this,"Invalid email pattern...",Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this,"Enter password...",Toast.LENGTH_LONG).show();
        }
        else {
            loginUser();
        }




    }

    private void loginUser() {
        progressDialog.setMessage("Logging In...");
        progressDialog.show();

        //login user firebase
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                //login success,check if user or admin
                checkUser();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                //login fail
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();

            }
        });
    }

    private void checkUser() {
        progressDialog.setMessage("Checking User...!");
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
                    // la user thi chuyen qua dashboard cho user
                    startActivity(new Intent(LoginActivity.this,DashboardUserActivity.class));
                    finish();
                }
                else if (userType.equals("admin")){
                    // la admin thi chuyen qua dashboard admin
                    startActivity(new Intent(LoginActivity.this,DashboardAdminActivity.class));
                    finish();

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

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
