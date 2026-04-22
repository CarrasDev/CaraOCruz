package com.example.caraocruz.utils

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log

class MusicManager(private val context: Context) {
    
    private var backgroundMusic: MediaPlayer? = null
    private var soundEffects: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isMusicEnabled = true
    
    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: MusicManager? = null
        
        fun getInstance(context: Context): MusicManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MusicManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Inicia la música de fondo
     */
    fun startBackgroundMusic() {
        Log.d("MusicManager", "startBackgroundMusic() called, isMusicEnabled: $isMusicEnabled")
        if (!isMusicEnabled) return
        
        try {
            // Liberar recursos anteriores
            backgroundMusic?.release()
            
            // Crear nuevo MediaPlayer para música de fondo
            Log.d("MusicManager", "Creating MediaPlayer for background music")
            backgroundMusic = MediaPlayer.create(context, com.example.caraocruz.R.raw.background_music)
            
            if (backgroundMusic != null) {
                backgroundMusic?.apply {
                    isLooping = true // Repetir música indefinidamente
                    setVolume(1.0f, 1.0f) // Volumen moderado para música de fondo
                    start()
                    Log.d("MusicManager", "Background music started successfully")
                }
            } else {
                Log.e("MusicManager", "Failed to create MediaPlayer for background music")
            }
        } catch (e: Exception) {
            Log.e("MusicManager", "Error starting background music", e)
            e.printStackTrace()
        }
    }
    
    /**
     * Detiene la música de fondo
     */
    fun stopBackgroundMusic() {
        backgroundMusic?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        backgroundMusic = null
    }
    
    /**
     * Pausa la música de fondo
     */
    fun pauseBackgroundMusic() {
        backgroundMusic?.apply {
            if (isPlaying) {
                pause()
            }
        }
    }
    
    /**
     * Reanuda la música de fondo
     */
    fun resumeBackgroundMusic() {
        if (isMusicEnabled) {
            backgroundMusic?.apply {
                if (!isPlaying) {
                    start()
                }
            }
        }
    }
    
    /**
     * Reproduce un efecto de sonido
     */
    fun playSoundEffect(soundResourceId: Int) {
        Log.d("MusicManager", "playSoundEffect() called with resource: $soundResourceId, isMusicEnabled: $isMusicEnabled")
        if (!isMusicEnabled) {
            Log.d("MusicManager", "Music is disabled, skipping sound effect")
            return
        }
        
        try {
            // Liberar efectos anteriores
            soundEffects?.release()
            
            Log.d("MusicManager", "Creating MediaPlayer for sound effect")
            soundEffects = MediaPlayer.create(context, soundResourceId)
            
            if (soundEffects != null) {
                soundEffects?.apply {
                    setVolume(1.0f, 1.0f) // Volumen máximo para efectos
                    start()
                    Log.d("MusicManager", "Sound effect started successfully")
                    
                    // Liberar recursos automáticamente cuando termine
                    setOnCompletionListener {
                        Log.d("MusicManager", "Sound effect completed, releasing resources")
                        it.release()
                        soundEffects = null
                    }
                }
            } else {
                Log.e("MusicManager", "Failed to create MediaPlayer for sound effect with resource: $soundResourceId")
            }
        } catch (e: Exception) {
            Log.e("MusicManager", "Error playing sound effect", e)
            e.printStackTrace()
        }
    }
    
    /**
     * Reproduce sonido de victoria
     */
    fun playWinSound() {
        playSoundEffect(com.example.caraocruz.R.raw.win_sound)
    }
    
    /**
     * Reproduce sonido de derrota
     */
    fun playLoseSound() {
        playSoundEffect(com.example.caraocruz.R.raw.lose_sound)
    }
    
    /**
     * Reproduce sonido de moneda
     */
    fun playCoinSound() {
        playSoundEffect(com.example.caraocruz.R.raw.coin_sound)
    }
    
    /**
     * Reproduce sonido de clic
     */
    fun playClickSound() {
        playSoundEffect(com.example.caraocruz.R.raw.click_sound)
    }
    
    /**
     * Activa/desactiva la música
     */
    fun setMusicEnabled(enabled: Boolean) {
        Log.d("MusicManager", "setMusicEnabled() called with enabled: $enabled")
        isMusicEnabled = enabled
        if (!enabled) {
            pauseBackgroundMusic()
        } else {
            resumeBackgroundMusic()
        }
    }
    
    /**
     * Verifica si la música está activada
     */
    fun isMusicEnabled(): Boolean = isMusicEnabled
    
    /**
     * Libera todos los recursos de audio
     */
    fun release() {
        backgroundMusic?.release()
        soundEffects?.release()
        backgroundMusic = null
        soundEffects = null
    }
}
