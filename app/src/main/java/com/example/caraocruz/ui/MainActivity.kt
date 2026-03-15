package com.example.caraocruz.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.caraocruz.R
import com.example.caraocruz.databinding.ActivityMainBinding
import com.example.caraocruz.data.AppDatabase
import com.example.caraocruz.data.JuegoRepository
import com.example.caraocruz.data.Partida
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: JuegoRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializamos la base de datos y el repositorio
        val dao = AppDatabase.getDatabase(this).juegoDao()
        repository = JuegoRepository(dao)

        // Configurar el botón de lanzar
        binding.btnLanzar.setOnClickListener {
            ejecutarJuego()
        }
    }

    private fun ejecutarJuego() {
        val apuestaTexto = binding.etApuesta.text.toString()

        if (apuestaTexto.isEmpty()) {
            Toast.makeText(this, "Introduce una apuesta", Toast.LENGTH_SHORT).show()
            return
        }

        val cantidadApostada = apuestaTexto.toInt()
        val esCaraSeleccionada = binding.rbCara.isChecked

        // 1. Lógica de Azar (Eficiencia de CPU)
        val resultadoEsCara = (0..1).random() == 0

        // 2. Actualizar Imagen (Recursos Estáticos)
        if (resultadoEsCara) {
            binding.ivMoneda.setImageResource(R.drawable.ic_cara)
        } else {
            binding.ivMoneda.setImageResource(R.drawable.ic_cruz)
        }

        // 3. Determinar si ganó
        val haGanado = (esCaraSeleccionada == resultadoEsCara)

        if (haGanado) {
            Toast.makeText(this, "¡Ganaste!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Perdiste...", Toast.LENGTH_SHORT).show()
        }

        // TODO: Aquí usaremos el repositorio con RxJava para actualizar las monedas en SQLite
    }
}