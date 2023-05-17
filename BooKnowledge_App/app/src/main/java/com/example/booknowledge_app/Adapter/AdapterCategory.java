package com.example.booknowledge_app.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booknowledge_app.Filter.FilterCategory;
import com.example.booknowledge_app.PdfListAdminActivity;
import com.example.booknowledge_app.R;
import com.example.booknowledge_app.model.ModelCategory;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.HolderCategory> implements Filterable {

    private Context context;
    public ArrayList<ModelCategory> categoryArrayList, filterList;

    HolderCategory holder;
    //instance of our filter class
    private FilterCategory filter;


    public AdapterCategory(Context context, ArrayList<ModelCategory> categoryArrayList) {
        this.context = context;
        this.categoryArrayList = categoryArrayList;
        this.filterList = categoryArrayList;
    }

    @NonNull
    @Override
    public HolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_category,parent,false);
        return new HolderCategory(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCategory holder, int position) {
        //get data
        ModelCategory model = categoryArrayList.get(position);
        String id = model.getId();
        String category = model.getCategory();
        String uid = model.getUid();
        String timestamp = model.getTimestamp();
        // set data
        holder.categoryTv.setText(category);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfListAdminActivity.class);
                intent.putExtra("categoryId",id);
                intent.putExtra("categoryTitle",category);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        if (categoryArrayList != null){
            return categoryArrayList.size();
        }
        return 0;
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterCategory(filterList,this);
        }
        return filter;
    }


    public class HolderCategory extends RecyclerView.ViewHolder {
        //ui view of row_category.xml
        public LinearLayout foreground;
        TextView categoryTv;
        RecyclerView rcv;
        public HolderCategory(View itemView) {
            super(itemView);
            //init ui view
            categoryTv = (TextView) itemView.findViewById(R.id.categoryTv);
            foreground = (LinearLayout) itemView.findViewById(R.id.foreground);
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
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });


    }

    public void removeItem(int index) {

        ModelCategory model = categoryArrayList.get(index);
        categoryArrayList.remove(index);
        notifyItemRemoved(index);
        deleteCategory(model, holder);

    }

}
