package com.example.bookapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bookapp.databinding.ActivityPdfDetailBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPdfDetailBinding

    //bookId
    private var bookId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get bookId
        bookId = intent.getStringExtra("bookId")!!
        //increase view count
        MyApplication.incrementBookViewCount(bookId)

        loadBookDetails()

        //back button click
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }
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
                    val title = "${snapshot.child("title").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"
                    val viewCnt = "${snapshot.child("viewCnt").value}"

                    //date format
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())
                    //load category
                    MyApplication.loadCategory(categoryId, binding.categoryTv)
                    //load thumbnail, pdf count
                    MyApplication.loadPdfFromUrlSinglePage(
                        "$url","$title", binding.pdfView, binding.progressBar, binding.pagesTv)
                    //load pdf size
                    MyApplication.loadPdfSize("$url", "$title", binding.sizeTv)

                    //set data
                    binding.titleTv.text = title
                    binding.descriptionTv.text = description
                    binding.viewCntTv.text = viewCnt
                    binding.downloadTv.text = downloadCnt
                    binding.dateTv.text = date
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}