package com.example.caraocruz.ui.juego

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.caraocruz.R
import com.example.caraocruz.databinding.FragmentJuegoBinding
import kotlinx.coroutines.launch

class JuegoFragment : Fragment(R.layout.fragment_juego) {

    private var _binding: FragmentJuegoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JuegoViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentJuegoBinding.bind(view)

        // Suscripción a los cambios en monedas:
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.monedas.collect { saldo ->
                binding.tvSaldo.txt = "Saldo: $saldo monedas"
            }
        }

        // Suscripción a los cambios en resultadoMensaje:
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.resultadoMensaje.collect { mensaje ->
                binding.tvMensaje.txt = mensaje
            }

        }

        binding.btnCara.setOnClickListener { procesarJugada(true) }
        binding.btnCruz.setOnClickListener { procesarJugada(false) }


    }

    private fun procesarJugada(esCara: Boolean) {
        val apuestaText = binding.etApuesta.text.toString()
        val apuesta = apuestaText.toIntOrNull() ?: 0
        viewModel.jugar(apuesta, esCara)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}