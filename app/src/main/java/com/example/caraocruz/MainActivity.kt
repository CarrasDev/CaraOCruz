package com.example.caraocruz

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.caraocruz.databinding.ActivityMainBinding
import com.example.caraocruz.databinding.ActivityPresentationBinding
import com.example.caraocruz.ui.juego.JuegoFragment
import com.example.caraocruz.ui.menu.HelpFragment
import com.example.caraocruz.ui.menu.HistoryFragment
import com.example.caraocruz.ui.menu.MusicSelectorFragment
import com.example.caraocruz.utils.MusicManager

class MainActivity : AppCompatActivity() {
    private lateinit var bindingMain: ActivityMainBinding
    private lateinit var bindingPresentation: ActivityPresentationBinding

    private lateinit var musicManager: MusicManager

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Inicializar MusicManager
        musicManager = MusicManager.getInstance(this)

        // Cargar layout de presentación
        bindingPresentation = ActivityPresentationBinding.inflate(layoutInflater)
        setContentView(bindingPresentation.root)

        // Cuando la presentación termine, se inicializa el layout principal:
        finishPresentation()

    }

    private fun finishPresentation() {
        // Cambiar al layout principal
        bindingMain = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindingMain.root)

        // Inicializar DrawerLayout, Toolbar, NavigationView, etc.
        initMainLayout()
    }

    private fun initMainLayout() {
        val drawerLayout = bindingMain.drawerLayout
        val navigationView = bindingMain.navigationView
        val toolbar = bindingMain.toolbar

        setSupportActionBar(toolbar)

        // Iniciar música de fondo
        musicManager.startBackgroundMusic()

        val toggle = androidx.appcompat.app.ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.open, R.string.close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Inicializar estado del menú de música
        navigationView.menu.findItem(R.id.nav_music)?.isChecked = musicManager.isMusicEnabled()

        // Cargar el fragmento JuegoFragment como fragment por defecto
        if (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, JuegoFragment())
                .commit()
        }

        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    while (supportFragmentManager.backStackEntryCount > 0) {
                        supportFragmentManager.popBackStackImmediate()
                    }
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, JuegoFragment())
                        .commit()
                }
                R.id.nav_history -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, HistoryFragment())
                        .addToBackStack(null)
                        .commit()

                }
                R.id.nav_help -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, HelpFragment())
                        .addToBackStack(null)
                        .commit()
                }
                R.id.nav_music_selector -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, MusicSelectorFragment())
                        .addToBackStack(null)
                        .commit()
                }
                R.id.nav_music -> {
                    // Toggle música activada/desactivada
                    val isCurrentlyEnabled = musicManager.isMusicEnabled()
                    musicManager.setMusicEnabled(!isCurrentlyEnabled)
                    it.isChecked = !isCurrentlyEnabled
                }
                /* TODO Para la siguiente versión
                R.id.nav_profile -> { /* Acción perfil */ }
                R.id.nav_settings -> { /* Acción configuración */ }
                */
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    override fun onResume() {
        super.onResume()
        musicManager.resumeBackgroundMusic()
    }

    override fun onPause() {
        super.onPause()
        musicManager.pauseBackgroundMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        musicManager.release()
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            return true
        }
        return super.onSupportNavigateUp()
    }
}
