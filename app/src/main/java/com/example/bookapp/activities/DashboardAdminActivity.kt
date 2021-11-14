package com.example.bookapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.example.bookapp.adapters.AdapterCategory
import com.example.bookapp.databinding.ActivityDashboardAdminBinding
import com.example.bookapp.models.ModelCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardAdminBinding
    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth
    // arraylist to hold categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    // adapter
    private lateinit var adapterCategory: AdapterCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadCategories()

        // search event
        binding.searchEt.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterCategory.filter.filter(s)
                }catch(e: Exception){

                }
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })

        // logout button click
        binding.logoutBtn.setOnClickListener{
            firebaseAuth.signOut()
            checkUser()
        }

        // add new category button click
        binding.addCategoryBtn.setOnClickListener{
            startActivity(Intent(this, CategoryAddActivity::class.java))
        }

        // add pdf button click
        binding.addPdfFab.setOnClickListener {
            startActivity(Intent(this, PdfAddActivity::class.java))
        }

        // profile button click
        binding.profileBtn.setOnClickListener{
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun loadCategories() {
        // initialize arraylist
        categoryArrayList = ArrayList()
        // get all saved categories from firebase db
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear list before adding data
                categoryArrayList.clear()
                for(ds in snapshot.children){
                    // get data
                    val model = ds.getValue(ModelCategory::class.java)
                    // add to arraylist
                    categoryArrayList.add(model!!)
                }
                // adapter
                adapterCategory = AdapterCategory(this@DashboardAdminActivity, categoryArrayList)
                // recyclerview
                binding.categoriesRv.adapter = adapterCategory
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }else{
            // put the email of the currentuser on the toolbar
            val email = firebaseUser.email
            binding.subtitleTv.text = email
        }
    }
}