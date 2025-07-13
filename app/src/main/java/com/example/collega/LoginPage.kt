package com.example.collega

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.collega.data.UserData
import com.example.collega.databinding.ActivityLoginPageBinding
import com.example.collega.di.FirebaseInstances
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.storage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Address
import java.util.UUID

class LoginPage : AppCompatActivity() {
    private lateinit var auth:FirebaseAuth
    private lateinit var binding: ActivityLoginPageBinding
    private lateinit var username:String
    private lateinit var tagline:String
    private lateinit var graduation:String
    private lateinit var email:String
    private lateinit var college:String
    private lateinit var work:String
    private lateinit var uid:String
    private lateinit var address:String
    private lateinit var profilepic:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
val ref = FirebaseDatabase.getInstance().reference
        binding = ActivityLoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

enableEdgeToEdge()
        val user = auth.currentUser
        val dataref = FirebaseDatabase.getInstance().reference
        binding.email.setText(intent.getStringExtra("email"))
        username =""
        tagline = ""
        graduation = ""
        email = binding.email.text.toString()
        college = ""
        work = ""
        uid =  ""
        address = ""
        profilepic = ""

        binding.password.setText(intent.getStringExtra("password"))


        binding.loginbtn.setOnClickListener {
            email = binding.email.text.toString()
            val password = binding.password.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val currentUser = auth.currentUser!!
                        if (!currentUser.isEmailVerified) {
                            Toast.makeText(this, "Please verify your email first.", Toast.LENGTH_LONG).show()
                        } else {
                            val userId = currentUser.uid
                            val ref = FirebaseDatabase.getInstance().reference

                            ref.child("users").child(userId)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            // Existing profile found
                                            val user = snapshot.getValue(UserData::class.java)
                                            if (user != null) {
                                                username = user.username
                                                tagline = user.tagline
                                                graduation = user.graduation
                                                email = user.email
                                                college = user.college
                                                work = user.work
                                                uid = user.userId
                                                address = user.address
                                                profilepic = user.profilePic
                                            }

                                            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                                                ref.child("usertokens").child(userId).child("token").setValue(token)
                                            }

                                            startActivity(Intent(this@LoginPage, MainActivity::class.java).putExtra("uid", userId))
                                            finish()
                                            Toast.makeText(this@LoginPage, "Successfully Logged in", Toast.LENGTH_SHORT).show()

                                        } else {
                                            // No profile found, set default and navigate
                                            val userData = UserData(
                                                userId,
                                                username.ifEmpty { "New User" },
                                                email,
                                                graduation,
                                                work,
                                                tagline,
                                                address,
                                                college,
                                                profilepic
                                            )
                                            ref.child("users").child(userId).setValue(userData).addOnSuccessListener {
                                                FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                                                    ref.child("usertokens").child(userId).child("token").setValue(token)
                                                }
                                                startActivity(Intent(this@LoginPage, MainActivity::class.java).putExtra("uid", userId))
                                                finish()
                                                Toast.makeText(this@LoginPage, "Complete your profile with login.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(this@LoginPage, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                                    }
                                })
                        }
                    } else {
                        Toast.makeText(this, "Login failed due to ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.forgotpass.setOnClickListener {
            val email = binding.email.text.toString()
            if (email.isNotEmpty())  {
                auth.sendPasswordResetEmail(email).addOnCompleteListener {
                    Toast.makeText(this, "Email send", Toast.LENGTH_SHORT).show()
                    val alertDialog = AlertDialog.Builder(this)
                        .setTitle("Reset password")
                        .setNeutralButton("Sure!", { dialog, which ->
                            dialog.dismiss()
                        })
                    alertDialog.create()
                    alertDialog.show()
                }
            } else{
                Toast.makeText(this, "Please enter email.", Toast.LENGTH_SHORT).show()
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}