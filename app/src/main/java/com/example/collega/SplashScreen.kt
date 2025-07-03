package com.example.collega

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.collega.databinding.ActivitySplashScreenBinding
import render.animations.Bounce
import render.animations.Fade
import render.animations.Render
import render.animations.Slide

class SplashScreen : AppCompatActivity() {
    private lateinit var binding:ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val logo = binding.splashlogoimg
        val splashtext = binding.splashnametext
        val render = Render(this)

        render.setAnimation(Bounce().In(logo))
        render.setDuration(1000)
        render.start()
        val rendertxt = Render(this)
        rendertxt.setAnimation(Slide().InDown(splashtext))
        rendertxt.setDuration(1000)
        rendertxt.start()
        Handler().postDelayed({
            startActivity(Intent(this,OnboardingPage::class.java))
            finish()
        },3000)
    }
}