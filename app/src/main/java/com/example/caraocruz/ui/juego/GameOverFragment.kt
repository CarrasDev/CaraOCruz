package com.example.caraocruz.ui.juego

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.caraocruz.R
import com.example.caraocruz.data.AppDatabase
import com.example.caraocruz.databinding.FragmentGameOverBinding

class GameOverFragment : Fragment(R.layout.fragment_game_over) {

    private var _binding: FragmentGameOverBinding? = null
    private val binding get() = _binding!!

    private val database by lazy { AppDatabase.getDatabase(requireContext()) }

    // Pedimos el ViewModel a la Activity principal
    private val viewModel: JuegoViewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            JuegoViewModelFactory(database)
        )[JuegoViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGameOverBinding.bind(view)

        // El botón lanza la orden a la base de datos (y lo desactivamos para evitar doble clic)
        binding.btnReiniciar.setOnClickListener {
            binding.btnReiniciar.isEnabled = false
            viewModel.reiniciarJuego()
        }

        // El botón de salir cierra la Activity entera
        binding.btnSalir.setOnClickListener {
            requireActivity().finish()
        }

        // ViewModel compartido.
        viewModel.juegoTerminado.observe(viewLifecycleOwner) { terminado ->
            if (terminado == false) {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}