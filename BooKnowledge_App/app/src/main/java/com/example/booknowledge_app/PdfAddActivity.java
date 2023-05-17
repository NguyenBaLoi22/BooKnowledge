package com.example.booknowledge_app;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.booknowledge_app.databinding.ActivityPdfAddBinding;
import com.example.booknowledge_app.model.ModelCategory;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfAddActivity extends AppCompatActivity {

     private ActivityPdfAddBinding binding;
     private ProgressDialog progressDialog;
     private FirebaseStorage firebaseStorage;
     private FirebaseAuth firebaseAuth;
     //arraylist to hold pdf categories
     private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;

     private Uri pdfUri = null;

     private static final int PDF_PICK_CODE = 1000;

     //tag de fix bug
     private static final String TAG = "ADD_PDF_TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        loadPdfCategories();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Hãy Đợi");
        progressDialog.setCanceledOnTouchOutside(false);

        //handle click
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 onBackPressed();
            }
        });

        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfPickIntent();
            }
        });

        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryPickDialog();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
                finish();
            }
        });
    }

    private String title= "", description ="", author ="";
    private void validateData() {
        //step1: validate data
        Log.d(TAG,"validateData :  ");

        //getdata
        author= binding.authorEt.getText().toString().trim();
        title = binding.titleEt.getText().toString().trim();
        description= binding.descriptionEt.getText().toString().trim();
        //validate data
        if(TextUtils.isEmpty(title)){
            Toast.makeText(this,"Nhập Tên Sách",Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this,"Nhập mô tả",Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(author)) {
            Toast.makeText(this,"Nhập tên tác giả",Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(selectedCategoryTitle)){
            Toast.makeText(this,"Chọn thể loại",Toast.LENGTH_SHORT).show();

        } else if (pdfUri == null) {
            Toast.makeText(this,"chọn tệp pdf",Toast.LENGTH_SHORT).show();

        }
        else {
            // tat ca du lieu nhap xong thi bay gio upload
            uploadPdfToStorage();
        }

    }

    private void uploadPdfToStorage() {
        //Step 2: upload pdf len storage
        Log.d(TAG,"uploadPdfToStorage : tải PDF lên bộ lưu trữ ");

        progressDialog.setMessage("Đang tải  PDF");
        progressDialog.show();

        String timestamp = String.valueOf(System.currentTimeMillis());

        //firebase path to pdf

        String filePathAndName = "Books/" + timestamp;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Log.d(TAG,"onSuccess: PDF UPLOAD TO STORAGE ");
                        Log.d(TAG,"onSuccess: Getting pdf url ");

                        Task<Uri> uriTask =taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedPdfUrl=""+uriTask.getResult();
                        //upload to firebase
                        uploadPdfIntoFireBase(uploadedPdfUrl,timestamp);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG,"onFailure: PDF UPLOAD TO STORAGE FAILED"+e.getMessage());
                        Toast.makeText(PdfAddActivity.this,"LỖI TẢI LÊN PDF LÊN BỘ LƯU TRỮ",Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void uploadPdfIntoFireBase(String uploadedPdfUrl, String timestamp) {
        //Step 3: upload pdf len firebase db
        Log.d(TAG,"uploadPdfToFireBase : uploading PDF info to firebase db ");
        progressDialog.setMessage("Tải lên thông tin pdf");

        String uid = firebaseAuth.getUid();

        //setup data to upload

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+uid);
        hashMap.put("id",""+timestamp);
        hashMap.put("title",""+title);
        hashMap.put("author",""+author);
        hashMap.put("description",""+description);
        hashMap.put("categoryId",""+selectedCategoryID);
        hashMap.put("url",""+uploadedPdfUrl);
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("viewsCount",0);
        hashMap.put("downloadCount",0);




        //db reference : path : Books

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onSuccess: Successfully uploaded");
                        Toast.makeText(PdfAddActivity.this,"onSuccess: Successfully uploaded",Toast.LENGTH_SHORT).show();
                        finish();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: Failed to upload to db due to"+e.getMessage());
                        Toast.makeText(PdfAddActivity.this,"onFailure: Failed to upload to db due to"+e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void loadPdfCategories() {
        Log.d(TAG,"loadPdfCategories: Loading pdf Categories ");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();
        //db reference to load categories ... path "Categories"

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();//clear before adding data
                categoryIdArrayList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    //get id va title of category
                    String categoryID = ""+ds.child("id").getValue();
                    String categoryTitle = ""+ds.child("category").getValue();

                    //add to respective arraylist
                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryID);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String selectedCategoryID, selectedCategoryTitle;
    private void categoryPickDialog() {
        Log.d(TAG,"categoryPickDialog: show category pick dialog ");

        //get array of categories form arraylist
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for(int i =0; i<categoryTitleArrayList.size();i++){
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn Thể Loại")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        //get click item form list
                        selectedCategoryTitle = categoryTitleArrayList.get(which);
                        selectedCategoryID   = categoryIdArrayList.get(which);

                        //set to category text view
                        binding.categoryTv.setText(selectedCategoryTitle);
                        Log.d(TAG,"onClick: selected category" + selectedCategoryID+ " " + selectedCategoryTitle );
                    }
                }).show();


    }

    private void pdfPickIntent() {
        Log.d(TAG, "PDFPickIntent: starting pdf picking intent ");
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select PDF"),PDF_PICK_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == PDF_PICK_CODE){
                Log.d(TAG,"onActivityResult: PDF picked");
                pdfUri = data.getData();
                Log.d(TAG,"onActivityResult: URI: "+pdfUri);
            }
        }else {
            Log.d(TAG,"onActivityResult: cancelled picking pdf");
            Toast.makeText(this,"Canceled picking pdf",Toast.LENGTH_SHORT).show();


        }
    }
}


