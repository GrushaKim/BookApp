package com.example.bookapp

import android.widget.Filter

class FilterPdfUser: Filter {
    // arrayList for search
    val filterList: ArrayList<ModelPdf>
    // adapter
    var adapterPdfUser: AdapterPdfUser

    constructor(filterList: ArrayList<Any>, adapterPdfUser: AdapterPdfUser) : super() {
        this.filterList = filterList
        this.adapterPdfUser = adapterPdfUser
    }

    override fun performFiltering(constraint: CharSequence): FilterResults {
        var constraint: CharSequence? = constraint
        val results = FilterResults()

        if(constraint != null && constraint.isNotEmpty()){
            constraint = constraint.toString().uppercase()
            val filteredModels = ArrayList<ModelPdf>()
            // validation
            for(i in filterList.indices){
                if(filterList[i].title.uppercase().contains(constraint)){
                    filteredModels.add(filterList[i])
                }
            }
            // return results
            results.count = filteredModels.size
            results.values = filteredModels

        }else{
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
       // apply filter change
        adapterPdfUser.pdfArrayList = results.values as ArrayList<ModelPdf>
        // notify
        adapterPdfUser.notifyDataSetChanged()
    }
}