package com.example.bookapp

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.databinding.RowCategoryBinding
import com.google.firebase.database.FirebaseDatabase

class AdapterCategory: RecyclerView.Adapter<AdapterCategory.HolderCategory>, Filterable {

    private val context: Context
    public var categoryArrayList: ArrayList<ModelCategory>
    private var filterList: ArrayList<ModelCategory>
    private var filter: FilterCategory? = null

    private lateinit var binding: RowCategoryBinding

    // constructor
    constructor(context: Context, categoryArrayList: ArrayList<ModelCategory>) {
        this.context = context
        this.categoryArrayList = categoryArrayList
        this.filterList = categoryArrayList
    }

    // inflate row_category
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
       binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderCategory(binding.root)
    }
    // data getter and setter w click handler
    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        // get data
        val model = categoryArrayList[position]
        val id = model.id
        val category = model.category
        val uid = model.uid
        val timestamp = model.timestamp
        // set data
        holder.categoryTv.text = category
        // delete btn click listener
        holder.deleteBtn.setOnClickListener{
            // alert message
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
                .setMessage("Do you want to delete?")
                .setPositiveButton("Confirm"){a, d ->
                    Toast.makeText(context, "Deleting", Toast.LENGTH_SHORT).show()
                    deleteCategory(model, holder)
                }
                .setNegativeButton("Cancel"){a, d ->
                    a.dismiss()
                }
                .show()
        }
    }

    private fun deleteCategory(model: ModelCategory, holder: HolderCategory) {
        var id = model.id
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Unable to delete. Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int {
        return categoryArrayList.size
    }

    // Viewholder for row_category UI
    inner class HolderCategory(itemView: View): RecyclerView.ViewHolder(itemView){
        // initialize ui view
        var categoryTv: TextView = binding.categoryTv
        var deleteBtn: ImageButton = binding.deleteBtn
    }

    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterCategory(filterList, this)
        }
        return filter as FilterCategory
    }

}