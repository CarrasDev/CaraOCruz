package com.example.caraocruz

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.caraocruz.databinding.ActivityLoginBinding
import com.example.caraocruz.utils.AuthManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = AuthManager.getInstance(this)

        // Si ya está logueado, vamos directamente a MainActivity
        if (authManager.isUserLoggedIn()) {
            startMainActivity()
            return
        }

        binding.btnGoogleSignIn.setOnClickListener {
            performSignIn()
        }

        updateUI()
    }

    private fun performSignIn() {
        lifecycleScope.launch {
            val result = authManager.signInWithGoogle(this@LoginActivity)
            result.onSuccess {
                startMainActivity()
            }.onFailure { e ->
                Toast.makeText(this@LoginActivity, getString(R.string.error_login_failed, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun updateUI() {
        val user = authManager.getCurrentUser()
        if (user != null) {
            binding.tvUserStatus.text = getString(R.string.login_status_logged, user.displayName ?: user.email)
            binding.btnGoogleSignIn.isEnabled = false
        } else {
            binding.tvUserStatus.text = getString(R.string.login_status_not_logged)
            binding.btnGoogleSignIn.isEnabled = true
        }
    }
}
