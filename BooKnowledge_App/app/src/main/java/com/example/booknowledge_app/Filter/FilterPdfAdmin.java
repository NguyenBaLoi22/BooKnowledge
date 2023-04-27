package com.example.booknowledge_app.Filter;

import android.widget.Filter;

import com.example.booknowledge_app.Adapter.AdapterCategory;
import com.example.booknowledge_app.Adapter.AdapterPdfAdmin;
import com.example.booknowledge_app.model.ModelCategory;
import com.example.booknowledge_app.model.ModelPdf;

import java.util.ArrayList;

public class FilterPdfAdmin extends Filter {

    //arraylist in which we want to the search

    ArrayList<ModelPdf> filterList;
    AdapterPdfAdmin adapterPdfAdmin;

    public FilterPdfAdmin(ArrayList<ModelPdf> filterList, AdapterPdfAdmin adapterPdfAdmin) {
        this.filterList = filterList;
        this.adapterPdfAdmin = adapterPdfAdmin;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //value should not be null and empty
        if(constraint != null && constraint.length()> 0){

            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelPdf> filteredModel = new ArrayList<>();

            for (int i=0; i< filterList.size(); i++){
                if (filterList.get(i).getTitle().toUpperCase().contains(constraint)){
                    //add to filter list
                    filteredModel.add(filterList.get(i));
                }
            }
            results.count = filteredModel.size();
            results.values = filteredModel;
        }
        else {
            results.count = filterList.size();
            results.values = filterList;

        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        //apply filter change
        adapterPdfAdmin.pdfArrayList= (ArrayList<ModelPdf>)results.values;

        //notify changes
        adapterPdfAdmin.notifyDataSetChanged();


    }
}
