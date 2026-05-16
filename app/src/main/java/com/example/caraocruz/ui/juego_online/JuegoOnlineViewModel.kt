package com.example.caraocruz.ui.juego_online

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.caraocruz.R
import com.example.caraocruz.data.Partida
import com.example.caraocruz.utils.AuthManager
import com.example.caraocruz.utils.FirestoreManager
import com.example.caraocruz.utils.MusicManager
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import kotlin.random.Random

class JuegoOnlineViewModel(context: Context) : ViewModel() {

    private val appContext = context.applicationContext
    private val firestoreManager = FirestoreManager.getInstance()
    private val authManager = AuthManager.getInstance(appContext)
    private val musicManager = MusicManager.getInstance(appContext)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)

    private val _monedas = MutableStateFlow(0L)
    val monedas: StateFlow<Long> = _monedas

    private val _resultadoMensaje = MutableSharedFlow<Int>(replay = 0)
    val resultadoMensaje: SharedFlow<Int> = _resultadoMensaje.asSharedFlow()

    private val _ultimoValor = MutableStateFlow(0)
    val ultimoValor: StateFlow<Int> = _ultimoValor

    private val _monedaImagenResId = MutableStateFlow(R.drawable.logocaraocruz)
    val monedaImagenResId: StateFlow<Int> = _monedaImagenResId

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _juegoTerminado = MutableStateFlow(false)
    val juegoTerminado: StateFlow<Boolean> = _juegoTerminado

    init {
        _resultadoMensaje.tryEmit(R.string.prompt_inicio)
        cargarSaldo()
    }

    private fun cargarSaldo() {
        val userId = authManager.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val saldo = firestoreManager.getOrInitializeUser(userId)
            _monedas.value = saldo
            comprobarFinDeJuego(saldo)
            _isLoading.value = false
        }
    }

    private fun comprobarFinDeJuego(saldo: Long) {
        _juegoTerminado.value = saldo <= 0
    }

    fun reiniciarJuego() {
        val userId = authManager.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val result = firestoreManager.reiniciarSaldo(userId)
            result.onSuccess { nuevoSaldo ->
                _monedas.value = nuevoSaldo
                _juegoTerminado.value = false
                _resultadoMensaje.emit(R.string.prompt_inicio)
                _ultimoValor.value = 0
                _monedaImagenResId.value = R.drawable.logocaraocruz
            }
            _isLoading.value = false
        }
    }

    fun prepararLanzamiento() {
        _monedaImagenResId.value = R.drawable.logocaraocruz
        viewModelScope.launch(Dispatchers.IO) {
            musicManager.playCoinSound()
        }
    }

    fun jugar(apuesta: Int, eleccionMoneda: Boolean) {
        val userId = authManager.getCurrentUser()?.uid ?: return

        if (apuesta <= 0) {
            _resultadoMensaje.tryEmit(R.string.msg_apuesta_cero)
            return
        }
        if (apuesta.toLong() > _monedas.value) {
            _resultadoMensaje.tryEmit(R.string.msg_sin_monedas)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            
            val resultadoEsCara = Random.nextBoolean()
            val gano = eleccionMoneda == resultadoEsCara
            val resultadoTexto = if (resultadoEsCara) "Cara" else "Cruz"

            // Actualizar imagen inmediatamente para la UI
            _monedaImagenResId.value = if (resultadoEsCara) R.drawable.cara else R.drawable.cruz
            _ultimoValor.value = apuesta

            // Obtener ubicación
            var lat: Double? = null
            var lon: Double? = null
            try {
                val location = fusedLocationClient.lastLocation.await()
                lat = location?.latitude
                lon = location?.longitude
            } catch (e: Exception) {
                Log.e("JuegoOnlineViewModel", "Error al obtener ubicación", e)
            }

            val partida = Partida(
                apuesta = apuesta,
                resultado = resultadoTexto,
                gano = gano,
                fecha = Date(),
                latitud = lat,
                longitud = lon
            )

            // Procesar en Firestore
            val result = firestoreManager.procesarJugada(userId, apuesta, gano, partida)
            
            result.onSuccess { nuevoSaldo ->
                _monedas.value = nuevoSaldo
                if (gano) {
                    _resultadoMensaje.emit(R.string.msg_ganaste)
                    musicManager.playWinSound()
                } else {
                    _resultadoMensaje.emit(R.string.msg_perdiste)
                    musicManager.playLoseSound()
                }
                comprobarFinDeJuego(nuevoSaldo)
            }.onFailure {
                _resultadoMensaje.emit(R.string.msg_error_db)
            }
            
            _isLoading.value = false
        }
    }

    fun toggleMusica(): Boolean {
        val newState = !musicManager.isMusicEnabled()
        musicManager.setMusicEnabled(newState)
        return newState
    }

    fun isMusicaActivada(): Boolean = musicManager.isMusicEnabled()
}

class JuegoOnlineViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JuegoOnlineViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JuegoOnlineViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
