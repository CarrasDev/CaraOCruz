package com.example.caraocruz.ui.menu

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.caraocruz.R
import com.example.caraocruz.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val sharedPrefs by lazy { 
        requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE) 
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        // Configuración de Geolocalización
        setupGeoSetting()
        
        // Configuración de Notificaciones
        setupNotifSetting()

        // Configuración de Captura de Pantalla
        setupScreenshotSetting()

        // Configuración de Calendario
        setupCalendarSetting()
    }

    private fun setupGeoSetting() {
        // Cargar estado guardado (por defecto true)
        val isGeoEnabled = sharedPrefs.getBoolean("geo_enabled", true)
        binding.switchGeo.isChecked = isGeoEnabled

        // Guardar cambios al pulsar el switch
        binding.switchGeo.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("geo_enabled", isChecked).apply()
        }
    }

    private fun setupNotifSetting() {
        // Cargar estado guardado (por defecto true)
        val isNotifEnabled = sharedPrefs.getBoolean("notif_enabled", true)
        binding.switchNotif.isChecked = isNotifEnabled

        // Guardar cambios al pulsar el switch
        binding.switchNotif.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("notif_enabled", isChecked).apply()
        }
    }

    private fun setupScreenshotSetting() {
        // Cargar estado guardado (por defecto true)
        val isScreenshotEnabled = sharedPrefs.getBoolean("screenshot_enabled", true)
        binding.switchScreenshot.isChecked = isScreenshotEnabled

        // Guardar cambios al pulsar el switch
        binding.switchScreenshot.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("screenshot_enabled", isChecked).apply()
        }
    }

    private fun setupCalendarSetting() {
        // Cargar estado guardado (por defecto true)
        val isCalendarEnabled = sharedPrefs.getBoolean("calendar_enabled", true)
        binding.switchCalendar.isChecked = isCalendarEnabled

        // Guardar cambios al pulsar el switch
        binding.switchCalendar.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("calendar_enabled", isChecked).apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
