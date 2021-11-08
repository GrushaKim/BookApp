package com.example.bookapp

import android.os.Bundle
import android.text.Editable
import android.text.Layout
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.example.bookapp.databinding.FragmentBooksUserBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BooksUserFragment : Fragment{
    //viewBinding for fragment_books.user.xml
    private lateinit var binding: FragmentBooksUserBinding

    companion object{
        private const val TAG = "BOOKS_USER_TAG"

        //get data from activity to load e-books and etc.
        fun newInstance(categoryId: String, category: String, uid: String): BooksUserFragment {
            val fragment = BooksUserFragment()
            //put data to bundle intent
            val args = Bundle()
            args.putString("categoryId", categoryId)
            args.putString("category", category)
            args.putString("uid", uid)
            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId = ""
    private var category = ""
    private var uid = ""

    private lateinit var pdfArrayList: ArrayList<ModelPdf>
    private lateinit var adapterPdfUser: AdapterPdfUser

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //get arguments passed from newInstance
        val args = arguments
        if(args != null){
            categoryId = args.getString("categoryId")!!
            category = args.getString("category")!!
            uid = args.getString("uid")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //inflate the layout for this fragment
        binding = FragmentBooksUserBinding.inflate(LayoutInflater.from(context), container, false)
        
        //load pdf by category with new instance
        Log.d(TAG, "onCreateView: the loaded category is $category")
        if(category == "All"){
            loadAllBooks()
        }else if(category == "Most Viewed"){
            loadMostViewedDownloadedBooks("viewCnt")
        }else if(category == "Most Downloaded"){
            loadMostViewedDownloadedBooks("downloadCnt")
        }else {
            loadCategorizedBooks()
        }

        //search
        binding.searchEt.addTextChangedListener { object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterPdfUser.filter.filter(s)
                }catch(e: Exception){
                    Log.d(TAG, "onTextChanged: Search Exception - ${e.message}")
                }
            }
            override fun afterTextChanged(s: Editable?) {
            }
        }}

        return binding.root
    }

    private fun loadAllBooks() {
        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list
                pdfArrayList.clear()
                for(ds in snapshot.children){
                    //get data
                    val model = ds.getValue(ModelPdf::class.java)
                    //add to list
                    pdfArrayList.add(model!!)
                }
                //setup
               adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                binding.booksRv.adapter = adapterPdfUser
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun loadMostViewedDownloadedBooks(orderBy: String) {
        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(orderBy).limitToLast(10) // load 10 most viewed or downloaded
            .addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list
                pdfArrayList.clear()
                for(ds in snapshot.children){
                    //get data
                    val model = ds.getValue(ModelPdf::class.java)
                    //add to list
                    pdfArrayList.add(model!!)
                }
                //setup
                adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                binding.booksRv.adapter = adapterPdfUser
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun loadCategorizedBooks() {
        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear list
                    pdfArrayList.clear()
                    for(ds in snapshot.children){
                        //get data
                        val model = ds.getValue(ModelPdf::class.java)
                        //add to list
                        pdfArrayList.add(model!!)
                    }
                    //setup
                    adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                    binding.booksRv.adapter = adapterPdfUser
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

}