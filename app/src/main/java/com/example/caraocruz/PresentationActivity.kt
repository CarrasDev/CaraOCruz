package com.example.caraocruz

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import com.example.caraocruz.databinding.ActivityPresentationBinding
import com.example.caraocruz.utils.AuthManager

class PresentationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPresentationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPresentationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Pre-carga del motor WebView para evitar tirones en el fragmento de Ayuda
        WebView(this).destroy()

        // Temporizador para pasar a la siguiente pantalla
        Handler(Looper.getMainLooper()).postDelayed({
            val authManager = AuthManager.getInstance(this)
            val destination = if (authManager.isUserLoggedIn()) {
                MainActivity::class.java
            } else {
                LoginActivity::class.java
            }
            
            val intent = Intent(this, destination)
            startActivity(intent)
            finish()
        }, 2500)        // 2,5 Segundos

    }
}