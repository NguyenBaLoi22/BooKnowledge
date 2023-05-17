package com.example.booknowledge_app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.booknowledge_app.Adapter.AdapterComment;
import com.example.booknowledge_app.Adapter.AdapterPdfFavorite;
import com.example.booknowledge_app.databinding.ActivityPdfDetailBinding;
import com.example.booknowledge_app.databinding.DialogCommentAddBinding;
import com.example.booknowledge_app.model.ModelComment;
import com.example.booknowledge_app.model.ModelPdf;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfDetailActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    boolean isInMyFavorites ;
    private ActivityPdfDetailBinding binding;
    private ArrayList<ModelComment> commentArrayList;
    private AdapterComment adapterComment;

    private ProgressDialog progressDialog;
    String bookId, bookTitle , bookUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get data from intent bookId
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Hãy Chờ");
        progressDialog.setCanceledOnTouchOutside(false);




        loadBookDetail();
        loadComments();

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            checkIsFavorites();
        }
        //increment book view count,whenever this page starts
        MyApplication.incrementBookViewCount(bookId);


        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.readBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(PdfDetailActivity.this,PdfViewActivity.class);
                intent1.putExtra("bookId",bookId);
                startActivity(intent1);

            }
        });

        binding.favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!isInMyFavorites){
                    //in favorites -> remove
                    MyApplication.addFavorite(PdfDetailActivity.this,bookId);
                }else {
                    // not in favorites list > add
                    MyApplication.removeFromFavorites(PdfDetailActivity.this, bookId);
                }

            }
        });

        binding.addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();

                if(firebaseUser==null){

                    Toast.makeText(PdfDetailActivity.this, "Cần phải đăng nhập để sử dụng tính năng này", Toast.LENGTH_SHORT).show();

                }else {
                    addCommentDialog();
                }

            }
        });
    }

    private void loadComments() {
        //init arraylist before adding data into it
        commentArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).child("comments")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear arraylist before add something
                        commentArrayList.clear();
                        for(DataSnapshot ds : snapshot.getChildren()){
                            //get data
                            ModelComment model = ds.getValue(ModelComment.class);
                            commentArrayList.add(model);
                        }
                        adapterComment  = new AdapterComment(PdfDetailActivity.this,commentArrayList);
                        binding.commentsRv.setAdapter(adapterComment);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private String comment="";
    private void addCommentDialog() {
        DialogCommentAddBinding commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this));

        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.CustomDialog);
        builder.setView(commentAddBinding.getRoot());

        //tao va show dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        commentAddBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();

            }
        });
        //btn them binh luan
        commentAddBinding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                comment = commentAddBinding.commentET.getText().toString().trim();
                //validate data
                if(TextUtils.isEmpty(comment)){
                    Toast.makeText(PdfDetailActivity.this, "Hãy thêm bình luận", Toast.LENGTH_SHORT).show();
                }else {
                    alertDialog.dismiss();
                    addComment();
                }
            }
        });

    }

    private void addComment() {
        progressDialog.setMessage("Đang thêm bình luận");
        progressDialog.show();

        //thoi gian cho id comment voi thoi gian comment
        String timestamp = ""+System.currentTimeMillis();

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("id",""+timestamp);
        hashMap.put("bookId",""+bookId);
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("comment",""+comment);
        hashMap.put("uid",""+firebaseAuth.getUid());

        // đường dẫn cơ sở dữ liệu để thêm dữ liệu vào nó
        // Books > bookId > comment > timestamp > commentdata
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).child("comments").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(PdfDetailActivity.this, " Thêm bình luận hoàn tất", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(PdfDetailActivity.this, " thêm bình luận thất bại do"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void loadBookDetail() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bookTitle = ""+ snapshot.child("title").getValue();
                        String description = ""+ snapshot.child("description").getValue();
                        String author = ""+ snapshot.child("author").getValue();
                        String categoryId = ""+ snapshot.child("categoryId").getValue();
                        String viewsCount = ""+ snapshot.child("viewsCount").getValue();
                        bookUrl = ""+ snapshot.child("url").getValue();
                        String timestamp = ""+ snapshot.child("timestamp").getValue();


                        String date = MyApplication.formatTimestamp(timestamp);
                        MyApplication.loadCategory(""+categoryId,binding.categoryTv);
                        MyApplication.loadPdfFromUrlSinglePage(""+bookUrl,""+bookTitle,binding.pdfView,binding.progressBar,binding.pagesTv);

                        binding.titleTv.setText(bookTitle);
                        binding.authorTv.setText(author);
                        binding.descriptionTv.setText(description);
                        binding.viewsTv.setText(viewsCount.replace("null","N/A"));
                        binding.dateTv.setText(date);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
    private void checkIsFavorites() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isInMyFavorites = snapshot.exists(); //true if exists , fail if not exists
                        if (isInMyFavorites) {
                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite, 0, 0);
                            binding.favoriteBtn.setText("Hủy Yêu Thích");
                        }else {
                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_border, 0, 0);
                            binding.favoriteBtn.setText("Yêu Thích");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}