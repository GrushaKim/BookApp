package com.example.bookapp.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.bookapp.databinding.ActivityPdfEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPdfEditBinding
    private companion object{
        private const val TAG = "PDF_EDIT_TAG"
    }
    // bookid from intent of AdapterPdfAdmin
    private var bookId = ""
    // progress dialog
    private lateinit var progressDialog: ProgressDialog
    // arraylist for category titles' holder
    private lateinit var categoryTitleArrayList: ArrayList<String>
    // arraylist for categoryId holder
    private lateinit var categoryIdArrayList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId")!!

        // setup progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        loadCategories()
        loadBookInfo()

        // backBtn click
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }
        // select category click
        binding.categoryTv.setOnClickListener{
            categoryDialog()
        }
        // update click
        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private fun loadBookInfo() {
        Log.d(TAG, "loadBookInfo: Loading book info")
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    // get book info
                    selectedCategoryId = snapshot.child("categoryId").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val title = snapshot.child("title").value.toString()

                    // set to views
                    binding.titleEt.setText(title)
                    binding.descriptionEt.setText(description)

                    // load book category info
                    Log.d(TAG, "onDataChange: Loading book categoryId")
                    val refBookCategory = FirebaseDatabase.getInstance().getReference("Categories")
                    refBookCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                // get category
                                val category = snapshot.child("category").value
                                // set to tv
                                binding.categoryTv.text = category.toString()
                            }
                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private var title = ""
    private var description = ""
    private fun validateData() {
        // get data from views
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        // validate data
        if (title.isEmpty()) {
            Toast.makeText(this, "Enter title", Toast.LENGTH_SHORT).show()
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Enter description", Toast.LENGTH_SHORT).show()
        } else if (selectedCategoryId.isEmpty()) {
            Toast.makeText(this, "Select category", Toast.LENGTH_SHORT).show()
        } else {
            updatePdf()
        }
    }

    private fun updatePdf() {
        Log.d(TAG, "updatePdf: Starting updating book info")
        progressDialog.setMessage("Updating book info")
        progressDialog.show()

        //setup data to update in db
        val hashMap = HashMap<String, Any>()
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"

        //start updating
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Log.d(TAG, "updatePdf: updated book info successfully")
                Toast.makeText(this, "Updated book info successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Log.d(TAG, "updatePdf: Failed to update book info. Error: ${e.message}")
                Toast.makeText(this, "Failed to update book info. Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryDialog() {
        // create a string array
        val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size)
        for(i in categoryTitleArrayList.indices){
            categoriesArray[i] = categoryTitleArrayList[i]
        }
        // alert
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select")
            .setItems(categoriesArray){dialog, position ->
                selectedCategoryId = categoryIdArrayList[position]
                selectedCategoryTitle = categoryTitleArrayList[position]

                binding.categoryTv.text = selectedCategoryTitle
            }
            .show()

    }

    private fun loadCategories() {
        Log.d(TAG, "loadCategories: loading categories")

        categoryTitleArrayList = ArrayList()
        categoryIdArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryIdArrayList.clear()
                categoryTitleArrayList.clear()
                for(ds in snapshot.children){
                    // get data
                    val id = "${ ds.child("id").value }"
                    val category = "${ds.child("category").value}"
                    // add it to list
                    categoryIdArrayList.add(id)
                    categoryTitleArrayList.add(category)

                    Log.d(TAG, "onDataChange: CategoryId - $id")
                    Log.d(TAG, "onDataChange: Category - $category")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

}