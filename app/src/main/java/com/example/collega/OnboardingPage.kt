package com.example.collega

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.collega.databinding.ActivityMainBinding
import com.example.collega.databinding.ActivityOnboardingPageBinding
import com.google.firebase.auth.FirebaseAuth

class OnboardingPage : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onStart() {
        auth = FirebaseAuth.getInstance()
        val cu = auth.currentUser
        if (cu!=null&&cu.isEmailVerified){
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
        super.onStart()
    }
    private lateinit var binding: ActivityOnboardingPageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            supportActionBar?.hide()
        binding = ActivityOnboardingPageBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.signupOnboard.setOnClickListener {
            startActivity(Intent(this,SignUpPage::class.java))

        }
        binding.loginOnboard.setOnClickListener {
            startActivity(Intent(this,LoginPage::class.java))
        }

    }
 }