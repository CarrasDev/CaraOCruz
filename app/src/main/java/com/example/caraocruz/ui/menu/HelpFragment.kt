package com.example.caraocruz.ui.menu

import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.example.caraocruz.R
import com.example.caraocruz.databinding.FragmentHelpBinding

class HelpFragment : Fragment(R.layout.fragment_help) {

    private var _binding: FragmentHelpBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHelpBinding.bind(view)

        // Configuración
        binding.webViewHelp.webViewClient = WebViewClient()
        binding.webViewHelp.settings.javaScriptEnabled = false

        // TODO: Cargo www.google.com para prueba, substituir por un archivo local
        binding.webViewHelp.loadUrl("https://www.google.com")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}