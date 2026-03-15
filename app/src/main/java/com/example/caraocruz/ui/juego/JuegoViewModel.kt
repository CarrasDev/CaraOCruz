package com.example.caraocruz.ui.juego

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class JuegoViewModel : ViewModel() {

    // Observables:
    private val _monedas = MutableStateFlow(200)    // TODO: Cargar desde la base de datos
    val monedas: StateFlow<Int> = _monedas

    private val _resultadoMensaje = MutableStateFlow("Introduce tu apuesta y elige")
    val resultadoMensaje: StateFlow<String> = _resultadoMensaje

    fun jugar(apuesta: Int, eleccionMoneda: Boolean) {
        if (apuesta <= 0) {
            _resultadoMensaje.value = "La apuesta debe ser mayor que cero"
            return
        }
        if (apuesta > _monedas.value) {
            _resultadoMensaje.value = "No tienes suficientes monedas"
            return
        }

        val resultadoEsCara = Random.nextBoolean()

        if (eleccionMoneda == resultadoEsCara) {
            _monedas.update { it + (apuesta * 2) }
            val ganancia = apuesta * 2
            _resultadoMensaje.value = "Has ganado $ganancia monedas"
        } else {
            _monedas.value -= apuesta
            _resultadoMensaje.value = "Has perdido $apuesta monedas"
        }

        // TODO: Añadir lógica para guardar las jugadas y el saldo en la base de datos


    }

}