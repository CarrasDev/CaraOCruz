package com.example.caraocruz

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.caraocruz.databinding.ActivityLoginBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        credentialManager = CredentialManager.create(this)

        // Si ya está logueado, vamos directamente a MainActivity
        if (auth.currentUser != null) {
            startMainActivity()
            return
        }

        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        updateUI()
    }

    private fun signInWithGoogle() {
        val webClientId = getString(R.string.default_web_client_id)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = this@LoginActivity,
                    request = request
                )
                
                val credential = result.credential
                Log.d("LoginActivity", "Credencial obtenida. Tipo: ${credential.type}")
                
                if (credential is GoogleIdTokenCredential) {
                    firebaseAuthWithGoogle(credential.idToken)
                } else if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
                } else {
                    Log.e("LoginActivity", "Tipo de credencial no esperado: ${credential.type}")
                    Toast.makeText(this@LoginActivity, getString(R.string.error_unsupported_account), Toast.LENGTH_SHORT).show()
                }
            } catch (e: GetCredentialException) {
                Log.e("LoginActivity", "Error al obtener credenciales", e)
                Toast.makeText(this@LoginActivity, getString(R.string.error_login_failed, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("LoginActivity", "Firebase Auth SUCCESS")
                    startMainActivity()
                } else {
                    Log.e("LoginActivity", "Firebase Auth FAILED", task.exception)
                    Toast.makeText(this, getString(R.string.error_firebase_auth, task.exception?.message), Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun updateUI() {
        val user = auth.currentUser
        if (user != null) {
            binding.tvUserStatus.text = getString(R.string.login_status_logged, user.displayName ?: user.email)
            binding.btnGoogleSignIn.visibility = View.GONE
        } else {
            binding.tvUserStatus.text = getString(R.string.login_status_not_logged)
            binding.btnGoogleSignIn.visibility = View.VISIBLE
        }
    }
}
