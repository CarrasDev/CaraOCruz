package com.example.caraocruz.ui.juego

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.caraocruz.R
import com.example.caraocruz.data.AppDatabase
import com.example.caraocruz.data.JuegoRepository
import com.example.caraocruz.databinding.FragmentJuegoBinding
import kotlinx.coroutines.launch

class JuegoFragment : Fragment(R.layout.fragment_juego) {

    private var _binding: FragmentJuegoBinding? = null
    private val binding get() = _binding!!

    private val database by lazy { AppDatabase.getDatabase(requireContext()) }
    private val repository by lazy { JuegoRepository(database.juegoDao()) }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // No es estrictamente necesario manejarlo aquí si el ViewModel lo gestiona con try-catch
    }

    private val viewModel: JuegoViewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            JuegoViewModelFactory(repository, requireContext())
        )[JuegoViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentJuegoBinding.bind(view)

        // Pedir permisos de ubicación y calendario al inicio
        requestPermissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.READ_CALENDAR,
                android.Manifest.permission.WRITE_CALENDAR
            )
        )

        // Suscripción a los cambios en saldo de moneda:
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.monedas.collect { saldo ->
                binding.tvSaldo.text = getString(R.string.label_saldo, saldo)
            }
        }

        // Suscripción a los cambios en resultadoMensaje:
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.resultadoMensaje.collect { resultadoId ->
                val mensaje = viewModel.ultimoValor.value

                if (resultadoId == R.string.msg_ganaste || resultadoId == R.string.msg_perdiste) {
                    binding.tvMensaje.text = getString(resultadoId, mensaje)
                } else {
                    binding.tvMensaje.text = getString(resultadoId)
                }
            }
        }

        // Suscripción al cambio imagen de moneda segun resultado
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.monedaImagenResId.collect { resId ->
                binding.ivMoneda.setImageResource(resId)
            }
        }

        // Suscripción al Fin de Juego
        viewModel.juegoTerminado.observe(viewLifecycleOwner) { terminado ->
            if (terminado == true) {
                mostrarDialogoFinDeJuego()
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

    private fun mostrarDialogoFinDeJuego() {
        // Navegamos a la pantalla de Game Over
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, GameOverFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}