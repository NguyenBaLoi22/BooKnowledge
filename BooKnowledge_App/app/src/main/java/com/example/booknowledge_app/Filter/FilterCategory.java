package com.example.booknowledge_app.Filter;

import android.widget.Filter;

import com.example.booknowledge_app.Adapter.AdapterCategory;
import com.example.booknowledge_app.model.ModelCategory;

import java.util.ArrayList;

public class FilterCategory extends Filter {

    //arraylist in which we want to the search

    ArrayList<ModelCategory> filterList;
    AdapterCategory adapterCategory;

    public FilterCategory(ArrayList<ModelCategory> filterList, AdapterCategory adapterCategory) {
        this.filterList = filterList;
        this.adapterCategory = adapterCategory;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //value should not be null and empty
        if(constraint != null && constraint.length()> 0){

            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelCategory> filteredModel = new ArrayList<>();

            for (int i=0; i< filterList.size(); i++){
                if (filterList.get(i).getCategory().toUpperCase().contains(constraint)){
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
        adapterCategory.categoryArrayList = (ArrayList<ModelCategory>)results.values;

        //notify changes
        adapterCategory.notifyDataSetChanged();


    }
}
