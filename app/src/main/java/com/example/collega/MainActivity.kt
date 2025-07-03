package com.example.collega

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.collega.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding  = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
enableEdgeToEdge()
//        val navHostController = findNavController(R.id.nav_host_fragment)
//        val bottomNavigationView = binding.bottombar
//        bottomNavigationView.setupWithNavController(navHostController)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Set up Bottom Navigation
        binding.bottombar.setupWithNavController(navController)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    fun hideBottomNavigation() {
        if(binding.bottombar.translationY == 0f) {
            binding.bottombar.animate()
                .translationY(binding.bottombar.height.toFloat())
                .setDuration(200)
        }

    }

    fun showBottomNavigation() {
        if (binding.bottombar.translationY >0f) {
            binding.bottombar.animate()
                .translationY(0f)
                .setDuration(200)
        }
    }
}