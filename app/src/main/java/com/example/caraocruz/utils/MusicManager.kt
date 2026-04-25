package com.example.caraocruz.utils

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log

class MusicManager(private val context: Context) {
    
    private var backgroundMusic: MediaPlayer? = null
    private var soundEffects: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isMusicEnabled = true
    private val sharedPrefs = context.getSharedPreferences("music_prefs", Context.MODE_PRIVATE)
    
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
        
        val customUriString = sharedPrefs.getString("custom_music_uri", null)
        
        try {
            // Liberar recursos anteriores
            backgroundMusic?.release()
            backgroundMusic = null
            
            // Intentar cargar música personalizada si existe
            if (customUriString != null) {
                try {
                    val uri = Uri.parse(customUriString)
                    backgroundMusic = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build()
                        )
                        setDataSource(context, uri)
                        isLooping = true
                        setVolume(1.0f, 1.0f)
                        prepare()
                        start()
                    }
                    Log.d("MusicManager", "Custom background music started successfully")
                    return // Éxito
                } catch (e: Exception) {
                    Log.e("MusicManager", "Failed to load custom music, falling back to default", e)
                    sharedPrefs.edit().remove("custom_music_uri").apply()
                }
            }

            // Cargar música por defecto
            Log.d("MusicManager", "Loading default background music")
            backgroundMusic = MediaPlayer.create(context, com.example.caraocruz.R.raw.background_music)
            backgroundMusic?.apply {
                isLooping = true
                setVolume(1.0f, 1.0f)
                start()
            }
            
        } catch (e: Exception) {
            Log.e("MusicManager", "Critical error starting background music", e)
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
        if (!isMusicEnabled) return
        
        try {
            soundEffects?.release()
            soundEffects = MediaPlayer.create(context, soundResourceId)
            soundEffects?.apply {
                setVolume(1.0f, 1.0f)
                start()
                setOnCompletionListener {
                    it.release()
                    if (soundEffects == it) soundEffects = null
                }
            }
        } catch (e: Exception) {
            Log.e("MusicManager", "Error playing sound effect", e)
        }
    }
    
    fun playWinSound() = playSoundEffect(com.example.caraocruz.R.raw.win_sound)
    fun playLoseSound() = playSoundEffect(com.example.caraocruz.R.raw.lose_sound)
    fun playCoinSound() = playSoundEffect(com.example.caraocruz.R.raw.coin_sound)
    fun playClickSound() = playSoundEffect(com.example.caraocruz.R.raw.click_sound)
    
    /**
     * Activa/desactiva la música
     */
    fun setMusicEnabled(enabled: Boolean) {
        isMusicEnabled = enabled
        if (!enabled) pauseBackgroundMusic() else resumeBackgroundMusic()
    }
    
    fun isMusicEnabled(): Boolean = isMusicEnabled

    fun setCustomBackgroundMusic(uri: Uri?) {
        sharedPrefs.edit().putString("custom_music_uri", uri?.toString()).apply()
        stopBackgroundMusic()
        startBackgroundMusic()
    }

    fun getCurrentMusicName(): String? {
        return sharedPrefs.getString("custom_music_uri", null)
    }
    
    fun release() {
        backgroundMusic?.release()
        soundEffects?.release()
        backgroundMusic = null
        soundEffects = null
    }
}
