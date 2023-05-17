package com.example.booknowledge_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.booknowledge_app.Adapter.AdapterPdfFavorite;
import com.example.booknowledge_app.databinding.ActivityProfileBinding;
import com.example.booknowledge_app.model.ModelPdf;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ProfileActivity extends AppCompatActivity {
    private ArrayList<ModelPdf> pdfArrayList;
    private AdapterPdfFavorite adapterPdfFavorite;
    private ActivityProfileBinding binding;
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "PROFILE_TAG";
    private FirebaseUser firebaseUser;
    private ProgressDialog progressDialog;

    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        firebaseAuth = FirebaseAuth.getInstance();
        //get current user
        firebaseUser = firebaseAuth.getCurrentUser();
        checkType2Hide();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Xin hãy đợi");
        progressDialog.setCanceledOnTouchOutside(false);

        //reset data of user info
        binding.accountStatusTV.setText("N/A");
        binding.memberDateTypeTV.setText("N/a");
        binding.favoriteBookCountTV.setText("N/a");
        binding.accountStatusTV.setText("N/a");

        binding.nav.setSelectedItemId(R.id.menu_profile);
        binding.nav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_profile:
                        return true;
                    case R.id.menu_home:
                        startActivity(new Intent(getApplicationContext(), DashboardUserActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.menu_setting:
                        startActivity(new Intent(getApplicationContext(), Setting_Activity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });


        loadFavoriteBook();
        loadUserInfo();


        //handle click
        binding.profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this , ProfileEditActivity.class));
            }
        });

        binding.editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this , ProfileEditActivity.class));
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //verify
        binding.accountStatusTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(firebaseUser.isEmailVerified()){
                    Toast.makeText(ProfileActivity.this, "Đã được xác minh", Toast.LENGTH_SHORT).show();
                }else{
                    // chua vetify
                    emailVerificationDialog();
                }
            }
        });
        
    }

    private void emailVerificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verify email")
                .setMessage("Bạn có chắc là bạn muốn gửi email xác nhân không?")
                .setPositiveButton("Gửi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendEmailVerification();
                        dialog.dismiss();

                    }
                }).setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                }).show();
    }

    private void sendEmailVerification() {
        progressDialog.setMessage("Đang gửi email xác nhận vào email của bạn" + firebaseUser.getEmail());
        progressDialog.show();


        firebaseUser.sendEmailVerification()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //gui thanh cong
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Gửi thành công, kiểm tra email của bạn :" +firebaseUser.getEmail(), Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Gửi thất bại vì"+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
        loadUserInfo();


    }

    private void loadUserInfo() {
        Log.d(TAG, "loadUserInfo: Loading user info of user " + firebaseAuth.getUid());

        //lấy dữ liệu xác thực email
        if(firebaseUser.isEmailVerified()){
            binding.accountStatusTV.setText("Đã xác thực");
        }else{
            binding.accountStatusTV.setText("Chưa xác thực");
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // lay tat ca thong tin user tai day

                        String email = ""+snapshot.child("email").getValue();
                        String name = ""+snapshot.child("name").getValue();
                        String profileImage = ""+snapshot.child("ProfileImage").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String uid = ""+snapshot.child("uid").getValue();
                        String userType = ""+snapshot.child("userType").getValue();

                        String formattedDate = MyApplication.formatTimestamp(timestamp);

                        //set data to UI

                        binding.mailTv.setText(email);
                        binding.nameTv.setText(name);
                        binding.memberDateTypeTV.setText(formattedDate);
                        binding.accountTypeTV.setText(userType);

                        //set image using glide
                        try {
                            Glide.with(ProfileActivity.this)
                                    .load(profileImage)
                                    .placeholder(R.drawable.ic_person_gray)
                                    .into(binding.profileIv);
                        }catch (Exception e ){
                            binding.profileIv.setImageResource(R.drawable.ic_person_gray);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void loadFavoriteBook() {
        pdfArrayList = new ArrayList<>();

        //load favorite books from database
        //Users > userId > Favorites
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before starting adding data
                        pdfArrayList.clear();
                        for(DataSnapshot ds : snapshot.getChildren()){
                            // get book id , and got other details in adapter using bookId
                            String bookId = ""+ ds.child("bookId").getValue();

                            ModelPdf modelPdf = new ModelPdf();
                            modelPdf.setId(bookId);

                            pdfArrayList.add(modelPdf);
                        }
                        //set number of countFavorites book
                        binding.favoriteBookCountTV.setText(""+pdfArrayList.size());
                        //set up adapter
                        adapterPdfFavorite = new AdapterPdfFavorite(ProfileActivity.this,pdfArrayList);
                        //set adapter to recyclerview
                        binding.bookRv.setAdapter(adapterPdfFavorite);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkType2Hide() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        //Check in database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        if (currentUser != null) {
            // Cập nhật thời gian gần nhất mà người dùng đã hoạt động trên ứng dụng của bạn
            usersRef.child(currentUser.getUid()).child("lastActiveTimestamp").setValue(System.currentTimeMillis());
        }
        ref.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                //get user type
                String userType = ""+snapshot.child("userType").getValue();
                //check user type
                if(userType.equals("user")){
                    binding.toolbarRL.setVisibility(View.INVISIBLE);

                }
                else if (userType.equals("admin")){
                    binding.nav.setVisibility(View.INVISIBLE);

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

}