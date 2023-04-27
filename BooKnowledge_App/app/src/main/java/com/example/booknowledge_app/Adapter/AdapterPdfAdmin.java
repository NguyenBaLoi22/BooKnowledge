package com.example.booknowledge_app.Adapter;

import static com.example.booknowledge_app.Constants.MAX_BYTES_PDF;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booknowledge_app.Filter.FilterPdfAdmin;
import com.example.booknowledge_app.MyApplication;
import com.example.booknowledge_app.PdfEditActivity;
import com.example.booknowledge_app.R;
import com.example.booknowledge_app.model.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {

    private Context context;
    private static final String TAG = "ADAPTER_PDF_TAG";
    public ArrayList<ModelPdf> pdfArrayList, filterList;
    private FilterPdfAdmin filter;
    private ProgressDialog progressDialog;


    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_pdf_admin,parent,false);
        return new HolderPdfAdmin(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {
        //get data
        ModelPdf model = pdfArrayList.get(position);
        String title = model.getTitle();
        String description = model.getDescription();
        String timestamp = model.getTimestamp();
        String pdfUrl = model.getUrl();
        String author = model.getAuthor();
        //convert format timestamp to dd/mm/yy

        String formattedDate = MyApplication.formatTimestamp(timestamp);
        //set data in recycle view
        holder.titleTV.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(formattedDate);
        holder.authorTv.setText("Tác giả: "+author);

        //Load further details like category , pdf from url, pdf size in separate function
        MyApplication.loadPdfSize("" + pdfUrl, "" + title, holder.sizeTv);
        loadPdfFromUrl(model,holder);
        loadCategory(model,holder);

        //holder  click, show dialog with 2 option edit and delete
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionDialog(model,holder);
            }
        });


    }

    private void moreOptionDialog(ModelPdf model, HolderPdfAdmin holder) {

        String bookId = model.getId();
//        String bookUrl = model.getUrl();
//        String bookTitle = model.getTitle();
        String[] options = {"Edit","Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Options")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle dialog option click
                        if (which == 0 ){
                            Intent intent = new Intent(context, PdfEditActivity.class);
                            intent.putExtra("bookId",bookId);
                            context.startActivity(intent);

                        }else if (which == 1){
                            deleteBook(model,holder);

                        }else {

                        }
                    }
                }).show();
    }

    private void deleteBook(ModelPdf model, HolderPdfAdmin holder) {
        String bookId = model.getId();
        String bookUrl = model.getUrl();
        String bookTitle = model.getTitle();

        Log.d(TAG, "deleteBook: Deleting...");
        progressDialog.setMessage("Deleting" + bookTitle+"...");
        progressDialog.show();
        Log.d(TAG, "deleteBook: Deleting from storage");
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Deleted from storage. Now deleting info from database");
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Books");
                        databaseReference.child(bookId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSuccess: Deleted from database too");
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "Book deleted Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Failed to delete from db due to "+e.getMessage());
                                        progressDialog.dismiss();
                                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to delete from storage due to" +e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });


    }


    private void loadPdfFromUrl(ModelPdf model, HolderPdfAdmin holder) {
        //using url we can get file and ts metadata from database
        String pdfUrl = model.getUrl();
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "onSuccess: "+model.getTitle()+ "successfully got the file");
                        //set to pdf view
                        holder.pdfView.fromBytes(bytes)
                                .pages(0)//show only first page
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        holder.progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onError: "+t.getMessage());
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        holder.progressBar.setVisibility(View.INVISIBLE);

                                        Log.d(TAG, "onPageError: "+t.getMessage());
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        holder.progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "loadComplete: pdf loaded  ");

                                    }
                                }).load();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        holder.progressBar.setVisibility(View.INVISIBLE);

                        Log.d(TAG, "onFailure: failed getting fire from url due to "+ e.getMessage());
                    }
                });
        
    }

    private void loadCategory(ModelPdf model, HolderPdfAdmin holder) {
        //get category using categoryId
        String categoryId = model.getCategoryId();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category = ""+snapshot.child("category").getValue();
                        //set to category text view
                        holder.categoryTv.setText(category);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterPdfAdmin(filterList,this);

        }
        return filter;
    }

    class HolderPdfAdmin extends RecyclerView.ViewHolder{

        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTV, descriptionTv, categoryTv,authorTv,dateTv,sizeTv;
        ImageButton moreBtn;
        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);
            //init ui views
            pdfView = itemView.findViewById(R.id.pdfView);
            progressBar = itemView.findViewById(R.id.progressBar);
            titleTV = itemView.findViewById(R.id.titleTv);
            descriptionTv = itemView.findViewById(R.id.descriptionTv);
            categoryTv = itemView.findViewById(R.id.categoryTv);
            authorTv = itemView.findViewById(R.id.authorTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            sizeTv = itemView.findViewById(R.id.sizeTv);

        }
    }
}
