package com.example.bookapp.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.bookapp.databinding.ActivityPdfAddBinding
import com.example.bookapp.models.ModelCategory
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPdfAddBinding
    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth
    // progress dialog
    private lateinit var progressDialog: ProgressDialog
    // arraylist to hold pdf categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    // uri of the pdf to be uploaded
    private var pdfUri: Uri? = null
    // TAG
    private val TAG = "PDF_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        loadPdfCategories()

        // init progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        // back button click
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }

        // category options click
        binding.categoryTv.setOnClickListener{
            categoryPickDialog()
        }

        // pick pdf intent
        binding.attPdfBtn.setOnClickListener{
            pdfPickIntent()
        }

        // start uploading pdf ebook
        binding.submitBtn.setOnClickListener {
            validateData()
        }


    }

    private var title = ""
    private var description = ""
    private var category = ""

    private fun validateData() {
        Log.d(TAG, "validateData: checking data")
        // get data
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()
        // validate data
        if(title.isEmpty()){
            Toast.makeText(this, "Enter book title", Toast.LENGTH_SHORT).show()
        }else if(description.isEmpty()){
            Toast.makeText(this, "Enter book description", Toast.LENGTH_SHORT).show()
        }else if(category.isEmpty()){
            Toast.makeText(this, "Select book category", Toast.LENGTH_SHORT).show()
        }else if(pdfUri == null){
            Toast.makeText(this, "Please attach file", Toast.LENGTH_SHORT).show()
        }else{
            // start uploading
            uploadPdfToStorage()
        }
    }

    private fun uploadPdfToStorage() {
        // upload pdf to firebase storage
        Log.d(TAG, "uploadPdfToStorage: uploading the attached file to firebase storage")
        progressDialog.setMessage("Uploading pdf file")
        progressDialog.show()

        val timestamp = System.currentTimeMillis()
        // pdf path in firebase storage
        val filePathAndName = "Books/$timestamp"
        // storage ref
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener {taskSnapshot ->
                Log.d(TAG, "uploadPdfToStorage: The file attached has been successfully uploaded")
                // get uri of the file
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while(!uriTask.isSuccessful);
                val uploadedPdfUrl = "${uriTask.result}"

                uploadPdfInfoToDb(uploadedPdfUrl, timestamp)
            }
            .addOnFailureListener{e ->
                Log.d(TAG, "uploadPdfToStorage: failed to upload. Error: ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload the attached file", Toast.LENGTH_SHORT).show()
            }


    }

    private fun uploadPdfInfoToDb(uploadedPdfUrl: String, timestamp: Long) {
        // upload pdf into to firebase db
        Log.d(TAG, "uploadPdfInfoToDb: uploading the file to db")
        progressDialog.setMessage("Uploading the pdf file")

        // get an uid of the current user
        val uid = firebaseAuth.uid
        // setup data to upload
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["url"] = "$uploadedPdfUrl"
        hashMap["timestamp"] = timestamp
        hashMap["viewCnt"] = 0
        hashMap["downloadCnt"] = 0
        // db ref
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "uploadPdfInfoToDb: uploaded to firebase db")
                progressDialog.dismiss()
                Toast.makeText(this, "Successfully uploaded to firebase db", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e ->
                Log.d(TAG, "uploadPdfInfoToDb: failed to upload into firebase. Error: ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload the attached file into firebase", Toast.LENGTH_SHORT).show()
            }



    }

    private fun loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Loading pdf categories")
        // initialize arraylist
        categoryArrayList = ArrayList()
        // get db reference
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear list before adding
                categoryArrayList.clear()
                for(ds in snapshot.children){
                    val model = ds.getValue(ModelCategory::class.java)
                    categoryArrayList.add(model!!)
                    Log.d(TAG, "onDataChange: ${model.category}")
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }   
        })
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryPickDialog(){
        Log.d(TAG, "categoryPickDialog: Showing pdf category pic dialog")
        //get string array of categories from arrayList
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for(i in categoryArrayList.indices){
            categoriesArray[i] = categoryArrayList[i].category
        }
        // alert
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select category")
            .setItems(categoriesArray){dialog, which ->
                // item click event
                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryId = categoryArrayList[which].id
                // set category to textview
                binding.categoryTv.text = selectedCategoryTitle
                Log.d(TAG, "categoryPickDialog: Selected Category Id: $selectedCategoryId, Title: $selectedCategoryTitle")
            }
            .show()
    }

    private fun pdfPickIntent(){
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent")
        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }

    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{result ->
            if(result.resultCode == RESULT_OK){
                Log.d(TAG, "PDF Picked")
                pdfUri = result.data!!.data
            }else{
                Log.d(TAG, "PDF Pick cancelled")
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }

    )
}