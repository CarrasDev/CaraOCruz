package com.example.caraocruz.ui.juego_online

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.caraocruz.R
import com.example.caraocruz.databinding.FragmentGameOverBinding
import kotlinx.coroutines.launch

class GameOverOnlineFragment : Fragment(R.layout.fragment_game_over) {

    private var _binding: FragmentGameOverBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JuegoOnlineViewModel by lazy {
        ViewModelProvider(
            requireActivity()
        )[JuegoOnlineViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGameOverBinding.bind(view)

        binding.btnReiniciar.setOnClickListener {
            binding.btnReiniciar.isEnabled = false
            viewModel.reiniciarJuego()
        }

        binding.btnSalir.setOnClickListener {
            requireActivity().finish()
        }

        // Observar estado de carga para el botón
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.btnReiniciar.isEnabled = !isLoading
            }
        }

        // Observar cuando el juego deja de estar terminado para volver
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.juegoTerminado.collect { terminado ->
                if (!terminado) {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
