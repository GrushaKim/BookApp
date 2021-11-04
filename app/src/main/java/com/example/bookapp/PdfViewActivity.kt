package com.example.bookapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.bookapp.databinding.ActivityPdfViewBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPdfViewBinding
    private companion object{
        const val TAG = "PDF_VIEW_TAG"
    }

    //bookId
    var bookId = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get bookId from PdfDetailActivity
        bookId = intent.getStringExtra("bookId")!!

        loadBookDetails()

        //back button click
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }
    }

    private fun loadBookDetails() {
        Log.d(TAG, "loadBookDetails: get pdf details from firebase db")
        //get url with bookId
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //book url
                    val pdfUrl = snapshot.child("url").value
                    Log.d(TAG, "onDataChange: PDF_URL -> $pdfUrl")
                    //load pdf
                    loadBookFromUrl("$pdfUrl")
                }
                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    private fun loadBookFromUrl(pdfUrl: String) {
        Log.d(TAG, "loadBookFromUrl: getting pdf from firebase storage with url")
        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
        reference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                Log.d(TAG, "loadBookFromUrl: pdf has been loaded from url")
                //loading
                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false) // for scroll vertical
                    .onPageChange{page, pageCount ->
                        val currentPage = page+1
                        binding.toolbarSubtitleTv.text = "$currentPage/$pageCount"
                        Log.d(TAG, "loadBookFromUrl: $currentPage/$pageCount")
                    }
                    .onError{ t ->
                        Log.d(TAG, "loadBookFromUrl: ${t.message}")
                    }
                    .onPageError { page, t ->
                        Log.d(TAG, "loadBookFromUrl: ${t.message}")
                    }
                    .load()
                binding.progressBar.visibility = View.GONE // != View.INVISIBLE
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "loadBookFromUrl: failed to get url. Error: ${e.message}")
                binding.progressBar.visibility = View.GONE
            }
    }
}