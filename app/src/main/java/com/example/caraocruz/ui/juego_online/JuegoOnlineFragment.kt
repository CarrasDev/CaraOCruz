package com.example.caraocruz.ui.juego_online

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.caraocruz.R
import com.example.caraocruz.databinding.FragmentJuegoOnlineBinding
import kotlinx.coroutines.launch

class JuegoOnlineFragment : Fragment(R.layout.fragment_juego_online) {

    private var _binding: FragmentJuegoOnlineBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JuegoOnlineViewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            JuegoOnlineViewModelFactory(requireContext())
        )[JuegoOnlineViewModel::class.java]
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentJuegoOnlineBinding.bind(view)

        requestPermissions()
        setupObservers()
        setupListeners()
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        requestPermissionLauncher.launch(permissions.toTypedArray())
    }

    private fun setupObservers() {
        // Saldo
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.monedas.collect { saldo ->
                binding.tvSaldo.text = getString(R.string.label_saldo, saldo)
            }
        }

        // Mensajes
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.resultadoMensaje.collect { resId ->
                val valor = viewModel.ultimoValor.value
                if (resId == R.string.msg_ganaste || resId == R.string.msg_perdiste) {
                    binding.tvMensaje.text = getString(resId, valor)
                } else {
                    binding.tvMensaje.text = getString(resId)
                }
            }
        }

        // Imagen Moneda
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.monedaImagenResId.collect { resId ->
                binding.ivMoneda.setImageResource(resId)
            }
        }

        // Bote Común
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.boteComun.collect { bote ->
                binding.tvBote.text = bote.toString()
            }
        }

        // Premio especial al ganar el bote
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.premioReciente.collect { premio ->
                if (premio > 0) {
                    Toast.makeText(requireContext(), getString(R.string.msg_ganaste_bote, premio), Toast.LENGTH_LONG).show()
                }
            }
        }

        // Loading state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.btnCara.isEnabled = !isLoading
                binding.btnCruz.isEnabled = !isLoading
            }
        }

        // Fin de juego Online
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.juegoTerminado.collect { terminado ->
                if (terminado) {
                    mostrarDialogoFinDeJuegoOnline()
                }
            }
        }
    }

    private fun mostrarDialogoFinDeJuegoOnline() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, GameOverOnlineFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun setupListeners() {
        binding.btnCara.setOnClickListener { procesarJugada(true) }
        binding.btnCruz.setOnClickListener { procesarJugada(false) }

        actualizarIconoMute()
        binding.fabMute.setOnClickListener {
            val enabled = viewModel.toggleMusica()
            actualizarIconoMute(enabled)
        }
    }

    private fun actualizarIconoMute(enabled: Boolean? = null) {
        val isMusicEnabled = enabled ?: viewModel.isMusicaActivada()
        val resId = if (isMusicEnabled) {
            android.R.drawable.ic_lock_silent_mode
        } else {
            android.R.drawable.ic_lock_silent_mode_off
        }
        binding.fabMute.setImageResource(resId)
    }

    private fun procesarJugada(esCara: Boolean) {
        val apuestaText = binding.etApuesta.text.toString()
        val apuesta = apuestaText.toIntOrNull() ?: 0

        if (apuesta <= 0 || apuesta.toLong() > viewModel.monedas.value) {
            viewModel.jugar(apuesta, esCara)
            return
        }

        binding.btnCara.isEnabled = false
        binding.btnCruz.isEnabled = false
        viewModel.prepararLanzamiento()

        binding.ivMoneda.rotationY = 0f
        binding.ivMoneda.animate()
            .rotationY(3600f)
            .setDuration(2000)
            .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
            .withEndAction {
                viewModel.jugar(apuesta, esCara)
                binding.btnCara.isEnabled = true
                binding.btnCruz.isEnabled = true
            }
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
