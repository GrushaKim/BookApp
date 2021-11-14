package com.example.bookapp.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.*
import com.example.bookapp.activities.PdfDetailActivity
import com.example.bookapp.activities.PdfEditActivity
import com.example.bookapp.databinding.RowPdfAdminBinding
import com.example.bookapp.filters.FilterPdfAdmin
import com.example.bookapp.models.ModelPdf

class AdapterPdfAdmin: RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin>, Filterable {

    //context
    private var context: Context
    //arraylist for pdf holder
    public var pdfArrayList: ArrayList<ModelPdf>
    //arraylist for filter
    private val filterList: ArrayList<ModelPdf>

    //viewBinding
    private lateinit var binding: RowPdfAdminBinding

    //filter object
    private var filter: FilterPdfAdmin? = null

    //constructor
    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
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
        MyApplication.loadCategory(categoryId, holder.categoryTv)
        MyApplication.loadPdfFromUrlSinglePage(
            pdfUrl,
            title,
            holder.pdfView,
            holder.progressBar,
            null
        )
        MyApplication.loadPdfSize(pdfUrl, title, holder.sizeTv)

        //more btn click navigating to edit and delete books
        holder.moreBtn.setOnClickListener{
            moreOptionsDialog(model, holder)
        }

        //item click for details
        holder.itemView.setOnClickListener{
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", pdfId)
            context.startActivity(intent)
        }
    }

    private fun moreOptionsDialog(model: ModelPdf, holder: HolderPdfAdmin) {
        //get id, url, title of the selected book
        val bookId = model.id
        val bookUrl = model.url
        val bookTitle = model.title

        //option dialog
        val options = arrayOf("Edit", "Delete")
        //alert
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Select")
            .setItems(options){ dialog, position ->
                if(position==0){
                    //edit
                    val intent = Intent(context, PdfEditActivity::class.java)
                    intent.putExtra("bookId", bookId)
                    context.startActivity(intent)
                }else if(position==1){
                    //delete
                    MyApplication.deleteBook(context, bookId, bookUrl, bookTitle)
                }
            }
            .show()
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterPdfAdmin(filterList, this)
        }
        return filter as FilterPdfAdmin
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