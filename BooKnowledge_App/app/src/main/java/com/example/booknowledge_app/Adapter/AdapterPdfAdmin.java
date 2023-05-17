package com.example.booknowledge_app.Adapter;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.booknowledge_app.Filter.FilterPdfAdmin;
import com.example.booknowledge_app.MyApplication;
import com.example.booknowledge_app.PdfDetailActivity;
import com.example.booknowledge_app.PdfEditActivity;
import com.example.booknowledge_app.R;
import com.example.booknowledge_app.model.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;


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
        progressDialog.setTitle("Xin Hãy Chờ");
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
        String pdfId = model.getId();
        String categoryId = model.getCategoryId();
        //convert format timestamp to dd/mm/yy
        String formattedDate = MyApplication.formatTimestamp(timestamp);
        //set data in recycle view
        holder.titleTV.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(formattedDate);
        holder.authorTv.setText(author);

        //Load further details like category , pdf from url, pdf size in separate function
        MyApplication.loadPdfSize("" + pdfUrl, "" + title, holder.sizeTv);
        MyApplication.loadPdfFromUrlSinglePage(""+pdfUrl,""+title,holder.pdfView,holder.progressBar,null);
        MyApplication.loadCategory(""+categoryId,holder.categoryTv);

        //holder  click, show dialog with 2 option edit and delete
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionDialog(model,holder);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId",pdfId);
                context.startActivity(intent);
            }
        });


    }

    private void moreOptionDialog(ModelPdf model, HolderPdfAdmin holder) {

        String bookId = model.getId();
        String bookUrl = model.getUrl();
        String bookTitle = model.getTitle();
        String[] options = {"Chỉnh Sửa","Xóa"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Chọn tùy chọn")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle dialog option click
                        if (which == 0 ){
                            Intent intent = new Intent(context, PdfEditActivity.class);
                            intent.putExtra("bookId",bookId);
                            context.startActivity(intent);

                        }else if (which == 1){
                           MyApplication.deleteBook(context,""+bookId,""+bookUrl,""+bookTitle);

                        }else {

                        }
                    }
                }).show();
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
