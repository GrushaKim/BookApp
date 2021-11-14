package com.example.bookapp.filters

import android.widget.Filter
import com.example.bookapp.adapters.AdapterCategory
import com.example.bookapp.models.ModelCategory

class FilterCategory: Filter {

    // arraylist to search
    private var filterList: ArrayList<ModelCategory>
    // adapter where filter needs to be implemented
    private var adapterCategory: AdapterCategory
    // constructor
    constructor(filterList: ArrayList<ModelCategory>, adapterCategory: AdapterCategory) : super() {
        this.filterList = filterList
        this.adapterCategory = adapterCategory
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()

        // value are NOT nullable and empty
        if(constraint != null && constraint.isNotEmpty()){
            // change to upper case
            constraint = constraint.toString().uppercase()
            val filteredModels: ArrayList<ModelCategory> = ArrayList()
            for(i in 0 until filterList.size){
                // validate data
                if(filterList[i].category.uppercase().contains(constraint)){
                    // add
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        } else{ // in case of null or empty
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        // apply filter changes
        adapterCategory.categoryArrayList = results.values as ArrayList<ModelCategory>
        // notify of them
        adapterCategory.notifyDataSetChanged()
    }

}