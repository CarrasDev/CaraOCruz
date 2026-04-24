package com.example.caraocruz.ui.juego

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.caraocruz.R
import com.example.caraocruz.data.AppDatabase
import com.example.caraocruz.data.JuegoRepository
import com.example.caraocruz.databinding.FragmentJuegoBinding
import com.example.caraocruz.utils.ImageUtils
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

        // Pedir permisos de ubicación, calendario y notificaciones al inicio
        val permissions = mutableListOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.READ_CALENDAR,
            android.Manifest.permission.WRITE_CALENDAR
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermissionLauncher.launch(permissions.toTypedArray())

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

                    if (resultadoId == R.string.msg_ganaste) {
                        mostrarDialogoCaptura()
                    }
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

        // Validación previa para no realizar la animación si la apuesta es inválida
        if (apuesta <= 0 || apuesta > viewModel.monedas.value) {
            viewModel.jugar(apuesta, esCara)
            return
        }

        // 1. Bloqueamos los botones para no aceptar más toques
        binding.btnCara.isEnabled = false
        binding.btnCruz.isEnabled = false

        // 2. Notificamos al ViewModel para reproducir el sonido y resetear la imagen (a través de StateFlow)
        viewModel.prepararLanzamiento()

        // 3. Reseteamos la rotación y animamos
        binding.ivMoneda.rotationY = 0f
        binding.ivMoneda.animate()
            .rotationY(10800f) // Gira 30 veces exactamente sobre el eje vertical (más rápido)
            .setDuration(3000)
            .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
            .withEndAction {
                // 4. Al terminar, ejecutamos la lógica en el ViewModel que actualizará la imagen final
                viewModel.jugar(apuesta, esCara)

                // 5. Desbloqueamos los botones
                binding.btnCara.isEnabled = true
                binding.btnCruz.isEnabled = true
            }
            .start()
    }

    private fun mostrarDialogoCaptura() {
        AlertDialog.Builder(requireContext())
            .setTitle("¡Victoria!")
            .setMessage("¿Deseas guardar una captura de pantalla de tu victoria en la galería?")
            .setPositiveButton("Sí") { _, _ ->
                val bitmap = ImageUtils.getScreenshotFromView(binding.root)
                val uri = ImageUtils.saveBitmapToGallery(
                    requireContext(),
                    bitmap,
                    "Victoria_${System.currentTimeMillis()}"
                )
                if (uri != null) {
                    Toast.makeText(requireContext(), "Captura guardada en la galería", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Error al guardar la captura", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("No", null)
            .show()
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