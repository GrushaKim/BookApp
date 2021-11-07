package com.example.bookapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.databinding.RowPdfUserBinding

class AdapterPdfUser: RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser> {

    //context
    private var context: Context
    //arrayList to hold pdfs
    var pdfArrayList: ArrayList<ModelPdf>
    //viewBinding for row_pdf_user.xml
    private lateinit var binding: RowPdfUserBinding

    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.pdfArrayList = pdfArrayList
    }
    //inflater
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfUser {
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfUser(binding.root)
    }
    //data getter+setter
    override fun onBindViewHolder(holder: HolderPdfUser, position: Int) {
        //get
        val model = pdfArrayList[position]
        val bookId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val uid = model.uid
        var url = model.url
        val timestamp = model.timestamp

        //convert time
        val date = MyApplication.formatTimeStamp(timestamp)

        //set
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = date

        MyApplication.loadPdfFromUrlSinglePage(url, title, holder.pdfView, holder.progressBar, null)
        MyApplication.loadCategory(categoryId, holder.categoryTv)
        MyApplication.loadPdfSize(url, title, holder.sizeTv)

        //open pdf detail button click
        holder.itemView.setOnClickListener{
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", bookId)
            context.startActivity(intent)
        }
    }
    override fun getItemCount(): Int {
        return pdfArrayList.size
    }
    //ViewHolder for row_pdf_user.xml
    inner class HolderPdfUser(itemView: View): RecyclerView.ViewHolder(itemView){
        //init UI components
        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var titleTv = binding.titleTv
        var descriptionTv = binding.descriptionTv
        var categoryTv = binding.categoryTv
        var sizeTv = binding.sizeTv
        var dateTv = binding.dateTv
    }


}