package com.example.caraocruz

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import com.example.caraocruz.databinding.ActivityPresentationBinding

class PresentationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPresentationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPresentationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Pre-carga del motor WebView para evitar tirones en el fragmento de Ayuda
        WebView(this).destroy()

        // Temporizador para pasar a la actividad principal
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 2500)        // 2,5 Segundos

    }
}