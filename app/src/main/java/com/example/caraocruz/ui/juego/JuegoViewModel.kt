package com.example.caraocruz.ui.juego

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.caraocruz.data.AppDatabase
import com.example.caraocruz.data.Partida
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.Date
import kotlin.random.Random

class JuegoViewModel(private val database: AppDatabase) : ViewModel() {

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
        val gano = eleccionMoneda == resultadoEsCara
        val resultadoTexto = if (resultadoEsCara) "Cara" else "Cruz"

        if (gano) {
            _monedas.update { it + (apuesta * 2) }
            val ganancia = apuesta * 2
            _resultadoMensaje.value = "Has ganado $ganancia monedas"
        } else {
            _monedas.value -= apuesta
            _resultadoMensaje.value = "Has perdido $apuesta monedas"
        }

        // Guardar la partida en la base de datos
        val partida = Partida(
            apuesta = apuesta,
            resultado = resultadoTexto,
            gano = gano,
            fecha = Date()
        )
        
        database.juegoDao().insertarPartida(partida)
            .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
            .subscribe()
    }

}

class JuegoViewModelFactory(
    private val database: AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JuegoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JuegoViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}