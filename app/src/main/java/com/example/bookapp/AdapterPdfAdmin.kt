package com.example.bookapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.databinding.RowPdfAdminBinding

class AdapterPdfAdmin: RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> {

    //context
    private var context: Context
    //arraylist for pdf holder
    private var pdfArrayList: ArrayList<ModelPdf>
    //viewBinding
    private lateinit var binding: RowPdfAdminBinding
    //constructor
    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
    }
    //inflate layout of row_pdf_admin.xml
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfAdmin(binding.root)
    }
    // data handling
    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) {
        // get data
        val model = pdfArrayList[position]
        val pdfId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val pdfUrl = model.url
        val timestamp = model.timestamp
        //convert timestamp to the selected format
        val formattedDate = MyApplication.formatTimeStamp(timestamp)
        //set data
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = formattedDate
        //get further details
        MyApplication.loadCategory(categoryId = categoryId, )

    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    // View Holder for row_pdf_admin.xml
    inner class HolderPdfAdmin(itemView: View): RecyclerView.ViewHolder(itemView){
        // init ui view
        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var titleTv = binding.titleTv
        var descriptionTv = binding.descriptionTv
        var categoryTv = binding.categoryTv
        var sizeTv = binding.sizeTv
        var dateTv = binding.dateTv
        var moreBtn = binding.moreBtn
    }


}