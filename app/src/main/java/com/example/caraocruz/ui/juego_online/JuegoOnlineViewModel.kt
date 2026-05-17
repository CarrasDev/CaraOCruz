package com.example.caraocruz.ui.juego_online

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.caraocruz.R
import com.example.caraocruz.data.Partida
import com.example.caraocruz.data.api.ApuestaRequest
import com.example.caraocruz.data.api.ApuestaResponse
import com.example.caraocruz.data.api.RetrofitClient
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

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

    private val _boteComun = MutableStateFlow(0L)
    val boteComun: StateFlow<Long> = _boteComun

    private val _premioReciente = MutableSharedFlow<Long>(replay = 0)
    val premioReciente: SharedFlow<Long> = _premioReciente.asSharedFlow()

    private val _onJuegoFinalizado = MutableSharedFlow<ApuestaResponse>()
    val onJuegoFinalizado: SharedFlow<ApuestaResponse> = _onJuegoFinalizado.asSharedFlow()

    private val _onJuegoError = MutableSharedFlow<Unit>()
    val onJuegoError: SharedFlow<Unit> = _onJuegoError.asSharedFlow()

    init {
        _resultadoMensaje.tryEmit(R.string.prompt_inicio)
        cargarSaldo()
        escucharBote()
    }

    private fun escucharBote() {
        firestoreManager.getBoteComunListener { nuevoBote ->
            // Actualizamos si es la carga inicial (valor actual 0) 
            // o si no estamos en medio de una apuesta (evita spoilers)
            if (_boteComun.value == 0L || !_isLoading.value) {
                _boteComun.value = nuevoBote
            }
        }
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

            try {
                // 1. Obtener Token de Autenticación
                val token = authManager.getIdToken() ?: throw Exception("No se pudo obtener el token")
                val bearerToken = "Bearer $token"

                // 2. Realizar llamada a la API (Cloud Function)
                val request = ApuestaRequest(userId, apuesta, eleccionMoneda)
                val response = RetrofitClient.instance.enviarApuesta(bearerToken, request)

                if (response.success) {
                    // Emitimos el resultado para que el Fragment decida cuándo mostrarlo
                    _onJuegoFinalizado.emit(response)
                } else {
                    Log.e("JuegoOnlineViewModel", "Error en API: ${response.error}")
                    _resultadoMensaje.emit(R.string.msg_error_db)
                    _onJuegoError.emit(Unit)
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e("JuegoOnlineViewModel", "Fallo en la llamada Retrofit", e)
                _resultadoMensaje.emit(R.string.msg_error_db)
                _onJuegoError.emit(Unit)
                _isLoading.value = false
            }
        }
    }

    fun finalizarProcesamientoResultado(response: ApuestaResponse, apuesta: Int) {
        viewModelScope.launch {
            // Actualizar UI con el resultado del servidor
            _monedaImagenResId.value = if (response.resultado == "Cara") R.drawable.cara else R.drawable.cruz
            _ultimoValor.value = apuesta
            _monedas.value = response.nuevoSaldo
            
            // Sincronizamos el bote con el valor real devuelto por la API para esta jugada
            _boteComun.value = response.nuevoBote

            if (response.gano) {
                _resultadoMensaje.emit(R.string.msg_ganaste)
                // Solo emitimos premioReciente si es un premio especial (mayor que el doble de la apuesta)
                // para evitar el spam del toast del "bote" en cada victoria normal.
                if (response.premio > apuesta * 2) {
                    _premioReciente.emit(response.premio)
                }
                musicManager.playWinSound()
            } else {
                _resultadoMensaje.emit(R.string.msg_perdiste)
                musicManager.playLoseSound()
            }
            comprobarFinDeJuego(response.nuevoSaldo)
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
