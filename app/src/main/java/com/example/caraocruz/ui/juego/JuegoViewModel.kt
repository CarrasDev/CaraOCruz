package com.example.caraocruz.ui.juego

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.caraocruz.data.AppDatabase
import com.example.caraocruz.data.Partida
import com.example.caraocruz.data.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.Date
import kotlin.random.Random
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class JuegoViewModel(private val database: AppDatabase) : ViewModel() {

    // Observables
    private val _monedas = MutableStateFlow(100)
    val monedas: StateFlow<Int> = _monedas

    private val _resultadoMensaje = MutableStateFlow("Introduce tu apuesta y elige")
    val resultadoMensaje: StateFlow<String> = _resultadoMensaje

    // Control de Fin de Juego
    private val _juegoTerminado = MutableLiveData<Boolean>()
    val juegoTerminado: LiveData<Boolean> get() = _juegoTerminado

    init {
        cargarSaldoInicial()
    }

    // Modificado para que la persistencia funcione
    private fun cargarSaldoInicial() {
        database.juegoDao().getUsuario()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ usuario ->
                if (usuario != null) {
                    // Cargamos el saldo real de la base de datos
                    _monedas.value = usuario.monedas

                    // Comprobamos si el usuario cerró la app estando arruinado
                    comprobarFinDeJuego(usuario.monedas)
                } else {
                    // Si el usuario no existe (primera vez que abre la app), le damos 100 monedas
                    reiniciarJuego()
                }
            }, {
                // Error silencioso
            })
    }

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

        // Beneficio 1 a 1
        if (gano) {
            _monedas.update { it + apuesta }
            _resultadoMensaje.value = "Has ganado $apuesta monedas"
        } else {
            _monedas.value -= apuesta
            _resultadoMensaje.value = "Has perdido $apuesta monedas"
        }

        //  Guardado completo: Partida + Saldo del Usuario
        val partida = Partida(
            apuesta = apuesta,
            resultado = resultadoTexto,
            gano = gano,
            fecha = Date()
        )
        val usuarioActualizado = Usuario(id = 1, monedas = _monedas.value)

        database.juegoDao().insertarPartida(partida)
            .andThen(database.juegoDao().guardarUsuario(usuarioActualizado))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                comprobarFinDeJuego(_monedas.value)
            }, {
                _resultadoMensaje.value = "Error al guardar en BD"
            })
    }

    private fun comprobarFinDeJuego(monedasActuales: Int) {
        if (monedasActuales <= 0) {
            _juegoTerminado.value = true
        }
    }

    fun reiniciarJuego() {
        val usuarioReiniciado = Usuario(id = 1, monedas = 100)

        database.juegoDao().guardarUsuario(usuarioReiniciado)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _monedas.value = 100
                _juegoTerminado.value = false
                _resultadoMensaje.value = "Introduce tu apuesta y elige"
            }, {})
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