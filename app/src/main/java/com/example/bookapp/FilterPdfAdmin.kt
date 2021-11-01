package com.example.bookapp

import android.widget.Filter

class FilterPdfAdmin: Filter {
    //arraylist to search
    var filterList: ArrayList<ModelPdf>
    //adapter where filter should be implemented
    var adapterPdfAdmin: AdapterPdfAdmin

    //constructor
    constructor(filterList: ArrayList<ModelPdf>, adapterPdfAdmin: AdapterPdfAdmin) : super() {
        this.filterList = filterList
        this.adapterPdfAdmin = adapterPdfAdmin
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint: CharSequence? = constraint
        val results = FilterResults()
        if(constraint != null && constraint.isNotEmpty()){
            // convert to lower case
            constraint = constraint.toString().lowercase()
            var filteredModels = ArrayList<ModelPdf>()
            for(i in filterList.indices){
                if(filterList[i].title.lowercase().contains(constraint)){
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        }else{
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        //apply filter changes
        adapterPdfAdmin.pdfArrayList = results.values as ArrayList<ModelPdf>
        //notify
        adapterPdfAdmin.notifyDataSetChanged()
    }

}