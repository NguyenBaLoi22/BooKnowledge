package com.example.booknowledge_app.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.booknowledge_app.Filter.FilterPdfUser;
import com.example.booknowledge_app.MyApplication;
import com.example.booknowledge_app.PdfDetailActivity;
import com.example.booknowledge_app.databinding.RowPdfUserBinding;
import com.example.booknowledge_app.model.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterPdfUser extends RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser> implements Filterable {
    private Context context;
    public ArrayList<ModelPdf> pdfArrayList, filterList;
    private FilterPdfUser filter;

    private RowPdfUserBinding binding;
    private static final String TAG = "PDF_ADAPTER_USER_TAG";


    public AdapterPdfUser(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;
    }


    @Override
    public HolderPdfUser onCreateViewHolder( ViewGroup parent, int viewType) {
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context), parent, false);

        return  new HolderPdfUser(binding.getRoot());
    }

    @Override
    public void onBindViewHolder( HolderPdfUser holder, int position) {
        ModelPdf model = pdfArrayList.get(position);
        String bookId = model.getId();
        String title = model.getTitle();
        String description = model.getDescription();
        String pdfUrl = model.getUrl();
        String category = model.getCategoryId();
        String timestamp = model.getTimestamp();
        String author = model.getAuthor();



        String date = MyApplication.formatTimestamp(timestamp);

        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(date);
        holder.authorTv.setText(author);

        MyApplication.loadPdfFromUrlSinglePage(""+ pdfUrl,""+title, holder.pdfView, holder.progressBar,null);
        MyApplication.loadCategory(""+category, holder.categoryTv);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId",bookId);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterPdfUser(filterList,this);
        }
        return filter;
    }

    class  HolderPdfUser extends RecyclerView.ViewHolder{


        TextView titleTv, descriptionTv, categoryTv, authorTv,dateTv;
        PDFView pdfView;
        ProgressBar progressBar;


        public HolderPdfUser( View itemView) {
            super(itemView);
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            authorTv = binding.authorTv;
            dateTv =binding.dateTv;
            pdfView=binding.pdfView;
            progressBar=binding.progressBar;

        }
    }
}
