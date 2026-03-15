package com.example.caraocruz.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.caraocruz.databinding.ActivityPresentationBinding

class PresentationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPresentationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPresentationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Temporizador para pasar a la actividad principal
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 4000)        // 4000 milisegundos

    }
}