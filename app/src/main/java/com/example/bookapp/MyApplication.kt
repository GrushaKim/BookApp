package com.example.bookapp

import android.app.Application
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.ktx.storageMetadata
import java.util.*

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
    }
    companion object{
        // create a static method to convert timestamp to the proper date format
        fun formatTimeStamp(timestamp: Long): String{
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp
            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }
        // get the uploaded file size
        fun loadPdfSize(pdfUrl: String, pdfTitle: String, sizeTv: TextView){
            val TAG = "PDF_SIZE_TAG"

            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener{
                    Log.d(TAG, "loadPdfSize: get metadata")
                    val bytes = StorageMetadata().sizeBytes.toDouble()
                    Log.d(TAG, "loadPdfSize: Size Bytes $bytes")
                    val kb = bytes/1024
                    val mb = kb/1024
                    if(mb>=1){
                        sizeTv.text = "${String.format("$.2f", mb)} MB"
                    }else if(kb>=1){
                        sizeTv.text = "${String.format("$.2f", kb)} KB"
                    }else {
                        sizeTv.text = "${String.format("$.2f", bytes)} bytes"
                    }
                }
                .addOnFailureListener{ e ->
                    Log.d(TAG, "loadPdfSize: Failed to get metadata, ERROR: ${e.message}")
                }
        }
        // get the file and its metadata from firebase storage with uri
        fun loadPdfFromUrlSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            pagesTv: TextView?
        ){

            val TAG = "PDF_THUMBNAIL_TAG"

            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener{ bytes ->
                    Log.d(TAG, "loadPdfSize: Size Bytes $bytes")
                    // Set pdfView
                    pdfView.fromBytes(bytes)
                        .pages(0)
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError { t->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onPageError{ page, t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onLoad { nbPages ->
                            progressBar.visibility = View.INVISIBLE
                            // if pagesTv param is not null
                            if(pagesTv != null){
                                pagesTv.text = "$nbPages"
                            }
                        }
                }
                .addOnFailureListener{ e ->
                    Log.d(TAG, "loadPdfSize: Failed to get metadata, ERROR: ${e.message}")
                }
        }
        // get category title with categoryId
        fun loadCategory(categoryId: String, categoryTv: TextView){
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val category: String = "${snapshot.child("category").value}"
                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                }
            )

        }
    }

}