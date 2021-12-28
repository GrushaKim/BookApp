package com.example.bookapp.activities

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.bookapp.Constants
import com.example.bookapp.MyApplication
import com.example.bookapp.R
import com.example.bookapp.adapters.AdapterComment
import com.example.bookapp.databinding.ActivityPdfDetailBinding
import com.example.bookapp.databinding.DialogCommentAddBinding
import com.example.bookapp.models.ModelComment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.FileOutputStream

class PdfDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPdfDetailBinding

    private companion object{
        const val TAG = "BOOK_DETAILS_TAG"
    }

    //bookId loaded from intent
    private var bookId = ""
    //from firebase
    private var bookTitle = ""
    private var bookUrl = ""
    private var isFavorite = false
    // adapter vars
    private lateinit var commentArrayList: ArrayList<ModelComment>
    private lateinit var adapterComment: AdapterComment


    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get bookId
        bookId = intent.getStringExtra("bookId")!!
        //increase view count
        MyApplication.incrementBookViewCount(bookId)

        loadBookDetails()
        loadComments()

        //progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        if(firebaseAuth.currentUser != null){
            //check if the book is favorite of the current user
            checkIsFavorite()
        }

        //back button click
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }

        //read pdf click
        binding.readBtn.setOnClickListener {
            val intent = Intent(this, PdfViewActivity::class.java)
            intent.putExtra("bookId", bookId)
            startActivity(intent)
        }

        //download pdf click
        binding.downloadBtn.setOnClickListener {
            // check permission
            if(ContextCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ){
                Log.d(TAG, "onCreate: STORAGE PERMISSION has already been granted")
                downloadBook()
            }else{
                Log.d(TAG, "onCreate: STORAGE_PERMISSION hasn't been granted")
                requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        //favorite click
        binding.favoriteBtn.setOnClickListener {
            //check login status
            if(firebaseAuth.currentUser == null){
                Toast.makeText(this, "You're not logged in", Toast.LENGTH_SHORT).show()
            }else{
                //check if the user added it to the fav
                if(isFavorite){
                    removeFromFavorite()
                }else{
                    addToFavorite()
                }
            }
        }

        //add comment click
        binding.addCommentBtn.setOnClickListener {
            //check login status
            if(firebaseAuth.currentUser == null){
                Toast.makeText(this, "You're not logged in", Toast.LENGTH_SHORT).show()
            }else{
               addCommentDialog()
            }
        }
    }

    private fun loadComments() {
        // init arr
        commentArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("comments")
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    // clear list
                    commentArrayList.clear()

                    for(ds in snapshot.children){
                        val comment = ds.getValue(ModelComment::class.java)
                        commentArrayList.add(comment!!)
                    }

                    // set adapter
                    adapterComment = AdapterComment(this@PdfDetailActivity, commentArrayList)
                    // set RecyclerView
                    binding.commentsRv.adapter = adapterComment
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private var comment = ""

    private fun addCommentDialog() {
        //inflate dialog_comment_add.xml
        val commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this))

        //setup alert dialog w transparent bg
        val builder = AlertDialog.Builder(this, R.style.customDialog)
        builder.setView(commentAddBinding.root)

        val alertDialog = builder.create()
        alertDialog.show()

        commentAddBinding.backBtn.setOnClickListener {
            alertDialog.dismiss()
        }

        // add comment to DB
        commentAddBinding.submitBtn.setOnClickListener {
            comment = commentAddBinding.commentEt.text.toString().trim()
            if(comment.isEmpty()){
                Toast.makeText(this, "Leave your comment!", Toast.LENGTH_SHORT).show()
            }else{
                alertDialog.dismiss()
                addComment()
            }
        }
    }

    private fun addComment() {
        //progress
        progressDialog.setMessage("Adding comment")
        progressDialog.show()

        //set data
        val timestamp = "${System.currentTimeMillis()}"
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["bookId"] = "$bookId"
        hashMap["timestamp"] = "$timestamp"
        hashMap["comment"] = "$comment"
        hashMap["uid"] = "${firebaseAuth.uid}"

        //update to DB
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("comments").child(timestamp)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Comment has been added", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "failed to add comment. Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }


    }

    private val requestStoragePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted: Boolean ->
            if(isGranted) {
                Log.d(TAG, "onCreate: STORAGE PERMISSION is granted")
                downloadBook()
            } else {
                Log.d(TAG, "onCreate: STORAGE PERMISSION is denied")
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun downloadBook(){
        Log.d(TAG, "downloadBook: Downloading the book")
        progressDialog.setMessage("Downloading the book")
        progressDialog.show()

        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        storageReference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                Log.d(TAG, "downloadBook: Successfully downloaded the book")
                saveToDownloadsFolder(bytes)
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "downloadBook: Failed to download the book. Error: ${e.message}")
                Toast.makeText(this, "Failed to download the book. Error: ${e.message}"
                    ,Toast.LENGTH_SHORT).show()
            }

    }

    private fun saveToDownloadsFolder(bytes: ByteArray) {
        Log.d(TAG, "saveToDownloadFolder: saving the downloaded book")

        val nameWithExtension = "${System.currentTimeMillis()}.pdf"
        try {
            val downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadFolder.mkdirs()
            val filePath = downloadFolder.path + "/" + nameWithExtension
            val out = FileOutputStream(filePath)
            out.write(bytes)
            out.close()

            Log.d(TAG, "saveToDownloadsFolder: Saved the file to Downloads")
            Toast.makeText(this, "Saved the file to Downloads", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
            incrementDownloadCount()

        }catch(e: Exception){
            progressDialog.dismiss()
            Log.d(TAG, "saveToDownloadsFolder: failed to save to Downloads. Error: ${e.message}")
            Toast.makeText(this, "Failed to save the file to Downloads. Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun incrementDownloadCount() {
        Log.d(TAG, "incrementDownloadCount: download cnt has been increased")
        //get the current downloads count
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var downloadCnt = "${snapshot.child("downloadCnt").value}"
                    Log.d(TAG, "onDataChange: Current Downloads Count: $downloadCnt")
                    if(downloadCnt=="" || downloadCnt=="null"){
                        downloadCnt = "0"
                    }
                    //increase
                    val newDownloadCnt = downloadCnt.toLong() +1
                    Log.d(TAG, "onDataChange: New Downloads Count: $newDownloadCnt")
                    //setup data to update
                    val hashMap = HashMap<String, Any>()
                    hashMap["downloadCnt"] = newDownloadCnt
                    //update to db
                    ref.child(bookId)
                        .updateChildren(hashMap)
                        .addOnSuccessListener { 
                            Log.d(TAG, "onDataChange: download cnt has been increased")
                        }
                        .addOnFailureListener { e ->
                            Log.d(TAG, "onDataChange: failed to increase download cnt. Error: ${e.message}")
                        }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadBookDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get data
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadCnt = "${snapshot.child("downloadCnt").value}"
                    bookTitle = "${snapshot.child("title").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val uid = "${snapshot.child("uid").value}"
                    bookUrl = "${snapshot.child("url").value}"
                    val viewCnt = "${snapshot.child("viewCnt").value}"

                    //date format
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())
                    //load category
                    MyApplication.loadCategory(categoryId, binding.categoryTv)
                    //load thumbnail, pdf count
                    MyApplication.loadPdfFromUrlSinglePage(
                        "$bookUrl",
                        "$bookTitle",
                        binding.pdfView,
                        binding.progressBar,
                        binding.pagesTv
                    )
                    //load pdf size
                    MyApplication.loadPdfSize("$bookUrl", "$bookTitle", binding.sizeTv)

                    //set data
                    binding.titleTv.text = bookTitle
                    binding.descriptionTv.text = description
                    binding.viewCntTv.text = viewCnt
                    binding.downloadTv.text = downloadCnt
                    binding.dateTv.text = date
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun checkIsFavorite(){
        Log.d(TAG, "checkIsFavorite: check if the book is in favorite books")
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    isFavorite = snapshot.exists()
                    if(isFavorite){
                        Log.d(TAG, "onDataChange: unavailable to add to favorite")
                        //set a filled fav icon if the book has already been added to favorite
                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0, R.drawable.ic_favorite_white, 0, 0
                        )
                        binding.favoriteBtn.text = "Remove from favorite"
                    }else{
                        Log.d(TAG, "onDataChange: available to add to favorite")
                        //set an outline fav icon if the book is not added to favorite
                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0, R.drawable.ic_favorite_border_white, 0, 0
                        )
                        binding.favoriteBtn.text = "Add to favorite"
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun addToFavorite() {
        Log.d(TAG, "addToFavorite: adding to favorite books")
        val timestamp = System.currentTimeMillis()

        //setup data to add
        val hashMap = HashMap<String, Any>()
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timestamp

        //save to db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "addToFavorite: added to favorite books")
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "addToFavorite: failed to add to favorite books. Error: ${e.message}")
                Toast.makeText(this, "Failed to add to favorite. Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeFromFavorite() {
        Log.d(TAG, "removeFromFavorite: removing from favorite books")

        //remove from db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "removeFromFavorite: removed from favorite books")
                Toast.makeText(this,"Successfully removed from favorite books.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "removeFromFavorite: failed to remove from favorite books. Error: ${e.message}")
                Toast.makeText(this,"Failed to remove from favorite books. Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}