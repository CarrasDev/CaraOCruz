package com.example.caraocruz.ui.juego

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class JuegoViewModel : ViewModel() {

    // Observables:
    private val _monedas = MutableStateFlow(200)    // 200 Monedas para empezar
    val monedas: StateFlow<Int> = _monedas

    private val _resultadoMensaje = MutableStateFlow("Introduce tu apùesta y elige")
    val resultadoMensaje: StateFlow<String> = _resultadoMensaje

    fun jugar(apuesta: Int, resultado: Boolean) {
        if (apuesta <= 0) {
            _resultadoMensaje.value = "La apuesta debe ser mayor que cero"
            return
        }
        if (apuesta > _monedas.value) {
            _resultadoMensaje.value = "No tienes suficientes monedas"
            return
        }

        val resultadoEsCara = Random.nextBoolean()
        // **************************************************************************
        val seleccionCara = false                       // TODO Fuerzo la elección de Cara
        // **************************************************************************

        if (seleccionCara == resultadoEsCara) {
            _monedas.update { it + (apuesta * 2) }
            val ganancia = apuesta * 2
            _resultadoMensaje.value = "Has ganado $ganancia monedas"
        } else {
            _monedas.value -= apuesta
            _resultadoMensaje.value = "Has perdido $apuesta monedas"
        }


    }

}