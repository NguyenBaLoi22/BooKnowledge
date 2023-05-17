package com.example.booknowledge_app.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booknowledge_app.MyApplication;
import com.example.booknowledge_app.PdfDetailActivity;
import com.example.booknowledge_app.databinding.RowPdfFavoritesBookBinding;
import com.example.booknowledge_app.model.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterPdfFavorite  extends RecyclerView.Adapter<AdapterPdfFavorite.HolderPdfFavorite> {

    private Context context;
    private ArrayList <ModelPdf> pdfArrayList;
    // view binding for row_favorite_book
    private RowPdfFavoritesBookBinding binding;
    private static final String TAG = "FAV_BOOk_TAG";

    // constructor


    public AdapterPdfFavorite(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfFavorite onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfFavoritesBookBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderPdfFavorite(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfFavorite holder, int position) {
        //get and set data
        ModelPdf model = pdfArrayList.get(position);

        loadBookDetail(model,holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId",model.getId());
                context.startActivity(intent);
            }
        });

        holder.removeFavoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.removeFromFavorites(context, model.getId());

            }
        });
    }

    private void loadBookDetail(ModelPdf model, HolderPdfFavorite holder) {
        String bookId = model.getId();
        Log.d(TAG, "loadBookDetail: Thông tin sách của sách :" + bookId);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get info

                        String bookTitle = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String bookUrl = ""+snapshot.child("url").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String author = ""+snapshot.child("author").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String uid = ""+snapshot.child("uid").getValue();
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();
                        String downloadCount = ""+snapshot.child("downloadCount").getValue(); // chua su dung, chuc nang doc off chua hoan thien
                        //set to model
                        model.setFavorite(true);
                        model.setTitle(bookTitle);
                        model.setDescription(description);
                        model.setTimestamp(timestamp);
                        model.setCategoryId(categoryId);
                        model.setUid(uid);
                        model.setUrl(bookUrl);
                        model.setAuthor(author);

                        String date = MyApplication.formatTimestamp(timestamp);

                        MyApplication.loadCategory(categoryId, holder.categoryTv);
                        MyApplication.loadPdfFromUrlSinglePage(""+bookUrl,""+bookTitle,holder.pdfView,holder.progressBar,null);

                        //set data to views
                        holder.titleTv.setText(bookTitle);
                        holder.authorTv.setText(author);
                        holder.descriptionTv.setText(description);
                        holder.dateTv.setText(date);



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {

        return pdfArrayList.size(); //return list size
    }

    //ViewHolder class
    class HolderPdfFavorite extends RecyclerView.ViewHolder{

        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv, categoryTv , authorTv, dateTv;
        ImageButton removeFavoriteBtn;
        public HolderPdfFavorite(@NonNull View itemView) {
            super(itemView);
            //init ui views row_pdf_favorite
            pdfView = binding.pdfView;
            titleTv = binding.titleTv;
            progressBar = binding.progressBar;
            removeFavoriteBtn = binding.removeFavoriteBtn;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            authorTv = binding.authorTv;
            dateTv = binding.dateTv;

        }
    }

}
