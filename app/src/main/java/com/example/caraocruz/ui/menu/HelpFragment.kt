package com.example.caraocruz.ui.menu

import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.example.caraocruz.R
import com.example.caraocruz.databinding.FragmentHelpBinding
import java.util.Locale

class HelpFragment : Fragment(R.layout.fragment_help) {

    private var _binding: FragmentHelpBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHelpBinding.bind(view)

        // Configuración del WebView
        binding.webViewHelp.webViewClient = WebViewClient()
        binding.webViewHelp.settings.javaScriptEnabled = false
        binding.webViewHelp.settings.domStorageEnabled = false
        binding.webViewHelp.settings.allowFileAccess = true

        // LOGICA MULTILENGUAJE

        // 1. Detectamos el idioma del sistema
        val idioma = Locale.getDefault().language

        // 2. Seleccionamos el fichero correspondiente según los HTMLs en assets
        val nombreFichero = when (idioma) {
            "en" -> "ayuda_en.html"
            "fr" -> "ayuda_fr.html"
            "de" -> "ayuda_de.html"
            "pt" -> "ayuda_pt.html"
            "it" -> "ayuda_it.html"
            "ca" -> "ayuda_ca.html"
            else -> "ayuda_es.html" // Idioma por defecto (Español)
        }

        // 3. Cargamos la URL dinámica
        binding.webViewHelp.loadUrl("file:///android_asset/$nombreFichero")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}