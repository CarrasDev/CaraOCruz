package com.example.caraocruz.ui.juego_online

import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.caraocruz.R
import com.example.caraocruz.databinding.FragmentJuegoOnlineBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class JuegoOnlineFragment : Fragment(R.layout.fragment_juego_online) {

    private var _binding: FragmentJuegoOnlineBinding? = null
    private val binding get() = _binding!!

    private var coinAnimator: ObjectAnimator? = null
    private var animationStartTime: Long = 0
    private val minAnimationDuration = 2000L

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
                actualizarEstadoControles(isLoading)
            }
        }

        // Resultado Juego Online (Sincronizado)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.onJuegoFinalizado.collectLatest { response ->
                val apuestaText = binding.etApuesta.text.toString()
                val apuesta = apuestaText.toIntOrNull() ?: 0
                
                val elapsed = System.currentTimeMillis() - animationStartTime
                val remaining = (minAnimationDuration - elapsed).coerceAtLeast(0)
                
                delay(remaining)
                detenerAnimacionYMostrarResultado(response, apuesta)
            }
        }

        // Error en Juego Online
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.onJuegoError.collect {
                detenerAnimacionPorError()
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

    private fun actualizarEstadoControles(isLoading: Boolean) {
        val isAnimating = coinAnimator != null
        if (isLoading) {
            binding.loadingOverlay.visibility = if (isAnimating) View.GONE else View.VISIBLE
            binding.btnCara.isEnabled = false
            binding.btnCruz.isEnabled = false
        } else {
            binding.loadingOverlay.visibility = View.GONE
            if (!isAnimating) {
                binding.btnCara.isEnabled = true
                binding.btnCruz.isEnabled = true
            }
        }
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

        iniciarAnimacionInfinita()
        animationStartTime = System.currentTimeMillis()

        viewModel.jugar(apuesta, esCara)
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

    private fun iniciarAnimacionInfinita() {
        coinAnimator?.cancel()
        binding.ivMoneda.rotationY = 0f
        coinAnimator = ObjectAnimator.ofFloat(binding.ivMoneda, "rotationY", 0f, 360f).apply {
            duration = 500
            repeatCount = ObjectAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun detenerAnimacionYMostrarResultado(response: com.example.caraocruz.data.api.ApuestaResponse, apuesta: Int) {
        coinAnimator?.cancel()
        coinAnimator = null // Importante para que actualizarEstadoControles sepa que no hay animación infinita
        
        val currentRotation = binding.ivMoneda.rotationY
        val targetRotation = currentRotation + (360f - (currentRotation % 360f)) + 720f
        
        binding.ivMoneda.animate()
            .rotationY(targetRotation)
            .setDuration(800)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    viewModel.finalizarProcesamientoResultado(response, apuesta)
                    ejecutarHaptico(response.gano)
                    actualizarEstadoControles(viewModel.isLoading.value)
                }
            })
            .start()
    }

    private fun ejecutarHaptico(gano: Boolean) {
        val feedback = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (gano) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.REJECT
        } else {
            HapticFeedbackConstants.LONG_PRESS
        }
        binding.ivMoneda.performHapticFeedback(feedback)
    }

    private fun detenerAnimacionPorError() {
        coinAnimator?.cancel()
        coinAnimator = null
        binding.ivMoneda.animate()
            .rotationY(0f)
            .setDuration(300)
            .start()
        actualizarEstadoControles(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coinAnimator?.cancel()
        _binding = null
    }
}
