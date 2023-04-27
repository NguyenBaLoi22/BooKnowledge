package com.example.booknowledge_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.example.booknowledge_app.Adapter.AdapterPdfAdmin;
import com.example.booknowledge_app.databinding.ActivityPdfListAdminBinding;
import com.example.booknowledge_app.model.ModelPdf;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PdfListAdminActivity extends AppCompatActivity {
    private ArrayList<ModelPdf> pdfArrayList;
    private ActivityPdfListAdminBinding binding;
    private AdapterPdfAdmin adapterPdfAdmin;
    private String categoryId, categoryTitle;
    private static final String TAG = "PDF_LIST_TAG";


    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfListAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get data from intent
        Intent intent = getIntent();
        categoryId = intent.getStringExtra("categoryId");
        categoryTitle =  intent.getStringExtra("categoryTitle");

        binding.subtitleTv.setText(categoryTitle);

        loadPdfList();
        //search
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //search as and when user type
                try{
                    adapterPdfAdmin.getFilter().filter(s);

                }catch (Exception e){
                    Log.d(TAG, "onTextChanged: " +e.getMessage());

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        //handle click,goto previous activity
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        
    }

    private void loadPdfList() {
        pdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("categoryId").equalTo(categoryId)

                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            //get data
                            ModelPdf model = ds.getValue(ModelPdf.class);
                            pdfArrayList.add(model);

                            Log.d(TAG, "onDataChange: "+model.getId()+ " "+model.getTitle());
                        }
                        adapterPdfAdmin = new AdapterPdfAdmin(PdfListAdminActivity.this, pdfArrayList);
                        binding.bookRv.setAdapter(adapterPdfAdmin);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}