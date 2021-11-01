package com.example.bookapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.example.bookapp.databinding.ActivityPdfListAdminBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfListAdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPdfListAdminBinding

    private var categoryId = ""
    private var category = ""

    private companion object{
        const val TAG = "PDF_LIST_ADMIN_TAG"
    }

    // arraylist for ebook holder
    private lateinit var pdfArrayList: ArrayList<ModelPdf>
    // adapter
    private lateinit var adapterPdfAdmin: AdapterPdfAdmin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfListAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get the passed info(category, id) from the intent
        val intent = intent
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!

        // set pdf category subtitle
        binding.subTitleTv.text = category
        // load e-books
        loadPdfList()
        // back button click
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }
        // search
        binding.searchEt.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) {
                //filtering
                try{
                    adapterPdfAdmin.filter!!.filter(s)
                }catch(e: Exception){
                    Log.d(TAG, "onTextChanged: ${e.message}")
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    private fun loadPdfList() {
        // init arrayList
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for(ds in snapshot.children){
                        // get data
                        val model = ds.getValue(ModelPdf::class.java)
                        // add it to list
                        if (model != null) {
                            pdfArrayList.add(model)
                            Log.d(TAG, "onDataChange: ${model.title} ${model.categoryId}")
                        }
                    }
                    // setup adapter
                    adapterPdfAdmin = AdapterPdfAdmin(this@PdfListAdminActivity, pdfArrayList)
                    binding.booksRv.adapter = adapterPdfAdmin
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}