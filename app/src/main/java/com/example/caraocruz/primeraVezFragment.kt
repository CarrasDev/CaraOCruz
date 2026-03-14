package com.example.caraocruz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.caraocruz.databinding.FragmentPrimeraVezBinding

class PrimeraVezFragment : Fragment() {

    private var _binding: FragmentPrimeraVezBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrimeraVezBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnContinue.setOnClickListener {
            val userName = binding.etUserName.text.toString().trim()

            if (userName.isNotEmpty()) {
                println("Nombre introducido: $userName")
                // Más adelante: guardar en SharedPreferences y cerrar fragment
            } else {
                binding.etUserName.error = getString(R.string.hint_user_name)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
