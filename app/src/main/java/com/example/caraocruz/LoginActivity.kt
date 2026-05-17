package com.example.caraocruz

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.exceptions.NoCredentialException
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
                val errorMessage = e.message ?: ""
                Log.e("LoginActivity", "Login failed", e)
                
                if (e is NoCredentialException || errorMessage.contains("no credentials available", ignoreCase = true)) {
                    // Si el error persiste tras haber intentado añadir cuenta, damos un mensaje más técnico
                    mostrarDialogoAnadirCuenta()
                } else {
                    Toast.makeText(this@LoginActivity, getString(R.string.error_login_failed, e.message), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarDialogoAnadirCuenta() {
        AlertDialog.Builder(this)
            .setTitle(R.string.title_no_google_account)
            .setMessage(R.string.msg_no_google_account)
            .setPositiveButton(R.string.btn_add_account) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_ADD_ACCOUNT)
                    intent.putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Añade una cuenta de Google en los Ajustes del sistema.", Toast.LENGTH_LONG).show()
                }
            }
            .setNeutralButton("Saber más") { _, _ ->
                mostrarAyudaTecnica()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun mostrarAyudaTecnica() {
        AlertDialog.Builder(this)
            .setTitle("Ayuda de Autenticación")
            .setMessage("Si ya tienes una cuenta en el dispositivo y este error persiste, es probable que la firma (SHA-1) de la aplicación no esté registrada en la consola de Firebase. \n\nEsto sucede a menudo al cambiar de ordenador de desarrollo.")
            .setPositiveButton("Entendido", null)
            .show()
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
