package com.example.caraocruz.ui.juego

import android.content.Context
import android.content.ContentValues
import android.provider.CalendarContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.caraocruz.R
import com.example.caraocruz.data.JuegoRepository
import com.example.caraocruz.data.Partida
import com.example.caraocruz.data.Usuario
import com.example.caraocruz.utils.MusicManager
import com.example.caraocruz.utils.NotificationHelper
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
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable

class JuegoViewModel(private val repository: JuegoRepository, context: Context) : ViewModel() {
    
    private val appContext = context.applicationContext
    private val musicManager = MusicManager.getInstance(appContext)
    private val notificationHelper = NotificationHelper(appContext)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)
    private val disposables = CompositeDisposable()

    // Observables
    private val _monedas = MutableStateFlow(100)
    val monedas: StateFlow<Int> = _monedas

    private val _resultadoMensaje = MutableSharedFlow<Int>(replay = 0)
    val resultadoMensaje: SharedFlow<Int> = _resultadoMensaje.asSharedFlow()
    private val _ultimoValor = MutableStateFlow(0)
    val ultimoValor: StateFlow<Int> = _ultimoValor
    private val _juegoTerminado = MutableLiveData<Boolean>()
    val juegoTerminado: LiveData<Boolean> get() = _juegoTerminado

    private val _monedaImagenResId = MutableStateFlow(R.drawable.logocaraocruz)
    val monedaImagenResId: StateFlow<Int> = _monedaImagenResId

    // Estado para el diálogo de captura (asegura que solo se muestre una vez)
    private val _mostrarCapturaDialogo = MutableSharedFlow<Boolean>(replay = 0)
    val mostrarCapturaDialogo: SharedFlow<Boolean> = _mostrarCapturaDialogo.asSharedFlow()


    init {
        _resultadoMensaje.tryEmit(R.string.prompt_inicio)
        cargarSaldoInicial()
    }

    // Modificado para que la persistencia funcione usando JuegoRepository
    private fun cargarSaldoInicial() {
        disposables.add(
            repository.getUsuario()
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
        )
    }

    fun prepararLanzamiento() {
        // Reseteamos la imagen a la genérica para que el StateFlow detecte el cambio después
        _monedaImagenResId.value = R.drawable.logocaraocruz

        // Reproducir sonido de moneda al hacer el lanzamiento
        viewModelScope.launch(Dispatchers.IO) {
            musicManager.playCoinSound()
        }
    }

    fun jugar(apuesta: Int, eleccionMoneda: Boolean) {
        if (apuesta <= 0) {
            _resultadoMensaje.tryEmit(R.string.msg_apuesta_cero)
            return
        }
        if (apuesta > _monedas.value) {
            _resultadoMensaje.tryEmit(R.string.msg_sin_monedas)
            return
        }

        val resultadoEsCara = Random.nextBoolean()
        val gano = eleccionMoneda == resultadoEsCara
        val resultadoTexto = if (gano) "Cara" else "Cruz"

        // Actualizar imagen de la moneda
        _monedaImagenResId.value = if (resultadoEsCara) {
            R.drawable.cara
        } else {
            R.drawable.cruz
        }

        _ultimoValor.value = apuesta

        // Beneficio 1 a 1
        if (gano) {
            _monedas.update { it + apuesta }
            viewModelScope.launch {
                _resultadoMensaje.emit(R.string.msg_ganaste)
            }
            viewModelScope.launch(Dispatchers.IO) {
                musicManager.playWinSound()
            }
            // Mostrar notificación de victoria en segundo plano
            viewModelScope.launch(Dispatchers.IO) {
                notificationHelper.showVictoryNotification(apuesta)
            }
            // Disparar el diálogo de captura
            viewModelScope.launch {
                _mostrarCapturaDialogo.emit(true)
            }
            // Guardar en el calendario si ha ganado
            guardarEnCalendario(apuesta)
        } else {
            _monedas.update { it - apuesta }
            viewModelScope.launch {
                _resultadoMensaje.emit(R.string.msg_perdiste)
            }
            viewModelScope.launch(Dispatchers.IO) {
                musicManager.playLoseSound()
            }
        }

        // Ejecutar en segundo plano para no bloquear la UI
        viewModelScope.launch {
            var lat: Double? = null
            var lon: Double? = null

            try {
                // Obtenemos la última ubicación conocida (más rápido que pedir una nueva)
                val location = fusedLocationClient.lastLocation.await()
                lat = location?.latitude
                lon = location?.longitude
            } catch (e: SecurityException) {
                // Sin permisos, se guarda como null
            } catch (e: Exception) {
                // Otros errores, se guarda como null
            }

            guardarPartidaConUbicacion(apuesta, resultadoTexto, gano, lat, lon)
        }
    }

    private fun guardarPartidaConUbicacion(apuesta: Int, resultadoTexto: String, gano: Boolean, lat: Double?, lon: Double?) {
        //  Guardado completo: Partida + Saldo del Usuario usando el repositorio
        val partida = Partida(
            apuesta = apuesta,
            resultado = resultadoTexto,
            gano = gano,
            fecha = Date(),
            latitud = lat,
            longitud = lon
        )
        val usuarioActualizado = Usuario(id = 1, monedas = _monedas.value)

        disposables.add(
            repository.insertarPartida(partida)
                .andThen(repository.guardarUsuario(usuarioActualizado))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    comprobarFinDeJuego(_monedas.value)
                }, {
                    _resultadoMensaje.tryEmit(R.string.msg_error_db)
                })
        )
    }

    private fun guardarEnCalendario(apuesta: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val values = ContentValues().apply {
                    put(CalendarContract.Events.DTSTART, System.currentTimeMillis())
                    put(CalendarContract.Events.DTEND, System.currentTimeMillis() + 60 * 60 * 1000) // 1 hora
                    put(CalendarContract.Events.TITLE, "¡Ganaste en Cara o Cruz!")
                    put(CalendarContract.Events.DESCRIPTION, "Has ganado $apuesta monedas.")
                    put(CalendarContract.Events.CALENDAR_ID, 1) // Usamos el ID 1 por defecto
                    put(CalendarContract.Events.EVENT_TIMEZONE, java.util.TimeZone.getDefault().id)
                }
                appContext.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            } catch (e: SecurityException) {
                // Sin permisos de calendario
            } catch (e: Exception) {
                // Otros errores
            }
        }
    }

    private fun comprobarFinDeJuego(monedasActuales: Int) {
        if (monedasActuales <= 0) {
            _juegoTerminado.value = true
        }
    }

    fun reiniciarJuego() {
        val usuarioReiniciado = Usuario(id = 1, monedas = 100)

        disposables.add(
            repository.guardarUsuario(usuarioReiniciado)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _monedas.value = 100
                    _juegoTerminado.value = false
                    _resultadoMensaje.tryEmit(R.string.prompt_inicio)
                    _ultimoValor.value = 0
                    _monedaImagenResId.value = R.drawable.logocaraocruz
                }, {})
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}

class JuegoViewModelFactory(
    private val repository: JuegoRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JuegoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JuegoViewModel(repository, context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
