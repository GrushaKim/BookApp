package com.example.bookapp.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.bookapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth
    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        // initialize progress dialog that will be shown while registering an account
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait with patience")
        progressDialog.setCanceledOnTouchOutside(false)

        // back button click event
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }

        // register button click
        binding.registerBtn.setOnClickListener{
            validateData()
        }
    }

    private var name = ""
    private var email = ""
    private var pwd = ""

    private fun validateData() {
        // 1. input data
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        pwd = binding.pwdEt.text.toString().trim()
        val confirmPwd = binding.confirmPwdEt.text.toString().trim()
        // 2. validate
        if(name.isEmpty()){
            Toast.makeText(this, "Enter your name", Toast.LENGTH_SHORT).show()
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Enter invalid email pattern", Toast.LENGTH_SHORT).show()
        }else if(pwd.isEmpty()){
            Toast.makeText(this, "Enter your password", Toast.LENGTH_SHORT).show()
        }else if(confirmPwd.isEmpty()){
            Toast.makeText(this, "Confirm your password", Toast.LENGTH_SHORT).show()
        }else if(pwd != confirmPwd){
            Toast.makeText(this, "Password doesn't match", Toast.LENGTH_SHORT).show()
        }else{
            createUserAccount()
        }

    }

    private fun createUserAccount() {
        // 3. create an account w firebase auth
        progressDialog.setMessage("We're creating your account!")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email, pwd)
            .addOnSuccessListener {
                updateUserInfo()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed creating account. Error: ${e.message}",
                Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserInfo() {
        // 4. save user info w firebase realtime database
        progressDialog.setMessage("Saving your info")

        val timestamp = System.currentTimeMillis()
        val uid = firebaseAuth.uid
        val hashMap: HashMap<String, Any?> = HashMap()
        // setup data to add
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileImage"] = ""
        hashMap["userType"] = "user"
        hashMap["timestamp"] = timestamp

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Your account has been created successfully",
                    Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, DashboardUserActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to save user info . Error: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }




    }
}