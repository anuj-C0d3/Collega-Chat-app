package com.example.collega

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.collega.databinding.ActivityPhotoVideoViewBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.squareup.picasso.Picasso

class PhotoVideoView : AppCompatActivity() {
    private lateinit var binding: ActivityPhotoVideoViewBinding
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPhotoVideoViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val img = intent.getStringExtra("img")
        val vdo = intent.getStringExtra("vdo")

        if (!img.isNullOrBlank()) {
            binding.imageView6.isVisible = true
            Picasso.get().load(img).into(binding.imageView6)
        }

        if (!vdo.isNullOrBlank()) {
            binding.video.isVisible = true
            player = ExoPlayer.Builder(this).build().also { exoPlayer ->
                binding.video.player = exoPlayer
                val mediaItem = MediaItem.fromUri(vdo)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }
}
