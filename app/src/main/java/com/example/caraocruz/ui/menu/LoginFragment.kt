package com.example.caraocruz.ui.menu

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.caraocruz.R
import com.example.caraocruz.databinding.FragmentLoginBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

        auth = Firebase.auth
        credentialManager = CredentialManager.create(requireContext())

        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        binding.btnSignOut.setOnClickListener {
            signOut()
        }

        updateUI()
    }

    private fun signInWithGoogle() {
        val webClientId = try {
            getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            "PONER_TU_CLIENT_ID_AQUI"
        }

        if (webClientId == "PONER_TU_CLIENT_ID_AQUI" || webClientId == "YOUR_CLIENT_ID_HERE") {
            Toast.makeText(requireContext(), "Error: Debes configurar el Web Client ID en strings.xml", Toast.LENGTH_LONG).show()
            return
        }

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
                    context = requireActivity(),
                    request = request
                )
                
                val credential = result.credential
                Log.d("LoginFragment", "Credencial obtenida con éxito. Clase: ${credential.javaClass.name}, Tipo: ${credential.type}")
                
                if (credential is GoogleIdTokenCredential) {
                    Log.d("LoginFragment", "ID Token obtenido: ${credential.idToken.take(10)}...")
                    firebaseAuthWithGoogle(credential.idToken)
                } else if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        Log.d("LoginFragment", "ID Token extraído de data: ${googleIdTokenCredential.idToken.take(10)}...")
                        firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
                    } catch (e: Exception) {
                        Log.e("LoginFragment", "Error al crear GoogleIdTokenCredential desde data", e)
                        Toast.makeText(requireContext(), "Error al procesar cuenta de Google", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("LoginFragment", "Tipo de credencial no esperado: ${credential.type}. Datos: ${credential.data}")
                    Toast.makeText(requireContext(), "Error: Tipo de cuenta no soportado (${credential.type})", Toast.LENGTH_SHORT).show()
                }
            } catch (e: GetCredentialException) {
                Log.e("LoginFragment", "Error al obtener credenciales", e)
                Toast.makeText(requireContext(), "Error al iniciar sesión: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        Log.d("LoginFragment", "Iniciando firebaseAuthWithGoogle")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d("LoginFragment", "Firebase Auth SUCCESS: ${user?.email}")
                    updateUI()
                    Toast.makeText(requireContext(), "Bienvenido ${user?.displayName}", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("LoginFragment", "Firebase Auth FAILED", task.exception)
                    Toast.makeText(requireContext(), "Error en Firebase Auth: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signOut() {
        auth.signOut()
        updateUI()
    }

    private fun updateUI() {
        val user = auth.currentUser
        if (user != null) {
            binding.tvUserStatus.text = "Conectado como: ${user.displayName ?: user.email}"
            binding.btnGoogleSignIn.visibility = View.GONE
            binding.btnSignOut.visibility = View.VISIBLE
        } else {
            binding.tvUserStatus.text = "No has iniciado sesión"
            binding.btnGoogleSignIn.visibility = View.VISIBLE
            binding.btnSignOut.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
