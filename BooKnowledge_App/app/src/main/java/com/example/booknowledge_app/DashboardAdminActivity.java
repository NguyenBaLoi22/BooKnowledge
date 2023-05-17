package com.example.booknowledge_app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booknowledge_app.Adapter.AdapterCategory;
import com.example.booknowledge_app.ItemTouchHelper.ItemTouchHelperListener;
import com.example.booknowledge_app.ItemTouchHelper.RecyclerViewItemTouchHelper;
import com.example.booknowledge_app.databinding.ActivityDashboardAdminBinding;
import com.example.booknowledge_app.model.ModelCategory;
import com.example.booknowledge_app.model.UserTime;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.divider.MaterialDividerItemDecoration;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardAdminActivity extends AppCompatActivity implements ItemTouchHelperListener {

    private ArrayList<ModelCategory> categoryArrayList;
    private AdapterCategory adapterCategory;
    private FirebaseAuth firebaseAuth;
    private ActivityDashboardAdminBinding binding;



    private Context context;



    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        getUserCounts();
        loadCategories();
        hide2buttonAdd();


        binding.addShowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.addCategoryBtn.getVisibility() == View.VISIBLE) {
                    hide2buttonAdd();
                }
                // Hiển thị điều khiển trên màn hình nếu chúng đang bị ẩn
                else {
                    show2buttonAdd();
                }

            }
        });

        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardAdminActivity.this,ProfileActivity.class));
            }
        });

        binding.addPdfFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardAdminActivity.this,PdfAddActivity.class));
            }
        });

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                checkUser();
            }
        });

        binding.addCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardAdminActivity.this,CategoryAddActivity.class));
            }
        });

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // dung khi user type
                try{
                    adapterCategory.getFilter().filter(s);
                }catch (Exception e){
                    Toast.makeText(DashboardAdminActivity.this,""+e,Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
            //them dong ngan 2 recyclerview
//        RecyclerView.ItemDecoration itemDecoration = new MaterialDividerItemDecoration(this,MaterialDividerItemDecoration.VERTICAL);
//        binding.categoriesRV.addItemDecoration(itemDecoration);
        ItemTouchHelper.SimpleCallback simpleCallback = new RecyclerViewItemTouchHelper(10,ItemTouchHelper.LEFT,this);
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(binding.categoriesRV);

    }

    private void hide2buttonAdd() {
        binding.addCategoryBtn.setVisibility(View.INVISIBLE);
        binding.addPdfFab.setVisibility(View.INVISIBLE);
    }

    private void show2buttonAdd() {
        binding.addCategoryBtn.setVisibility(View.VISIBLE);
        binding.addPdfFab.setVisibility(View.VISIBLE);
    }

    private void loadCategories() {

        categoryArrayList = new ArrayList<>();
        // Lấy tham chiếu tới nút "users" trên Firebase Realtime Database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    ModelCategory model = ds.getValue(ModelCategory.class);
                    //add to arraylist
                    categoryArrayList.add(model);
                }
                adapterCategory = new AdapterCategory(DashboardAdminActivity.this, categoryArrayList);
                binding.categoriesRV.setAdapter(adapterCategory);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void checkUser() {
        //get current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser==null){
            //not logged in, goto main screen
            startActivity(new Intent(DashboardAdminActivity.this,MainActivity.class));
            finish();
        }else {
            //logged in, get user info
            String email = firebaseUser.getEmail();
            //set in textview toolbar
            binding.title.setText(email);
        }
    }

    private void deleteCategory(ModelCategory model, AdapterCategory.HolderCategory holder){
        String id = model.getId();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(id)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //delete succesfully
                        Toast.makeText(context,"Đã Xóa Thành Công",Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }
    @Override
    public void onSwipe(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof AdapterCategory.HolderCategory){
//            String CategoryDelete = categoryArrayList.get(viewHolder.getAdapterPosition()).getCategory();
//            final ModelCategory categorydelete = categoryArrayList.get(viewHolder.getAdapterPosition());
            final int indexDelete = viewHolder.getAdapterPosition();
            adapterCategory.removeItem(indexDelete);
        }

    }

    @SuppressLint("SetTextI18n")
    private void updateUserCounts(int userCount, int onlineUserCount, int offlineUserCount) {
        // Hiển thị số lượng người dùng, số người dùng đang online và số người dùng offline trên giao diện người dùng
        TextView userCountTextView = binding.CountUsers;
        userCountTextView.setText("Tổng :"+String.valueOf(userCount));

        TextView onlineUserCountTextView = binding.CountUsersOnline;
        onlineUserCountTextView.setText("Online :"+String.valueOf(onlineUserCount));

        TextView offlineUserCountTextView = binding.CountUsersOffline;
        offlineUserCountTextView.setText("Offline :"+ String.valueOf(offlineUserCount));
    }

    private void getUserCounts() {
        // Lấy tham chiếu tới nút "users" trên Firebase Realtime Database
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // Đếm tổng số lượng người dùng
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int userCount = (int) dataSnapshot.getChildrenCount();

                // Đếm số người dùng đang online và số người dùng offline
                int onlineUserCount = 0;
                int offlineUserCount = 0;

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    UserTime user = userSnapshot.getValue(UserTime.class);
                    if (user != null) {
                        // Nếu thời gian gần nhất mà người dùng đã hoạt động cách hiện tại không quá 20 phút
                        if (System.currentTimeMillis() - user.getLastActiveTimestamp() <= 20 * 60 * 1000) {
                            onlineUserCount++;
                        } else {
                            offlineUserCount = userCount - onlineUserCount;
                        }
                    }
                }

                // Hiển thị số lượng người dùng, số người dùng đang online và số người dùng offline trên giao diện người dùng
                updateUserCounts(userCount, onlineUserCount, offlineUserCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
    }
}