package com.example.collega

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.collega.databinding.ActivitySignUpPageBinding
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth


class SignUpPage : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpPageBinding
    private lateinit var firebaseAuth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
enableEdgeToEdge()
        binding = ActivitySignUpPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        binding.signupbtn.setOnClickListener {
            val username = binding.signupUserName.text.toString()
            val email = binding.emailid.text.toString()
            if ( binding.apppassword.isHovered) {
                Toast.makeText(this, "Enter min 6 character password", Toast.LENGTH_SHORT).show()
            }
            val password = binding.apppassword.text.toString()
         
            val confirmpass = binding.confirmpassword.text.toString()
            if(username.isEmpty()||email.isEmpty()||password.isEmpty()||confirmpass.isEmpty()){
                Toast.makeText(this, "Please fill all the required field", Toast.LENGTH_SHORT).show()
            } else if(password != confirmpass) {
                Toast.makeText(this, "Confirm password not matched!", Toast.LENGTH_SHORT).show()
            }else {
                firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
                   it-> if(it.isSuccessful) {
                    firebaseAuth.currentUser!!.sendEmailVerification(ActionCodeSettings.zzb())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val alertDialog = AlertDialog.Builder(this)
                                    .setTitle("Email verification")
                                    .setMessage("Click on the verification send on given email $email")
                                    .setPositiveButton(
                                        "Okay!",
                                        DialogInterface.OnClickListener { dialog, which ->
                                            val intent = Intent(this, LoginPage::class.java)
                                            intent.putExtra("email", email)
                                            intent.putExtra("password", password)
                                            intent.putExtra("username", username)
                                            startActivity(intent)
                                            finish()
                                        })
                                    .create()
                                    .show()
                            } else {
                                Toast.makeText(this, "Verification failed", Toast.LENGTH_SHORT)
                                    .show()
                                Toast.makeText(
                                    this,
                                    "Enter valid email address.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Please enter 6 character password.", Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "SignUp Failed due to ${it.exception?.message}", Toast.LENGTH_SHORT).show()
                
                }
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}