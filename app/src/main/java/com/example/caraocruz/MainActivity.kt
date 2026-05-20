package com.example.caraocruz

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.caraocruz.databinding.ActivityMainBinding
import com.example.caraocruz.ui.juego.JuegoFragment
import com.example.caraocruz.ui.juego_online.JuegoOnlineFragment
import com.example.caraocruz.ui.menu.HelpFragment
import com.example.caraocruz.ui.menu.HistoryFragment
import com.example.caraocruz.ui.menu.MusicSelectorFragment
import com.example.caraocruz.ui.menu.RankingFragment
import com.example.caraocruz.ui.menu.SettingsFragment
import com.example.caraocruz.utils.MusicManager
import com.example.caraocruz.utils.AuthManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var bindingMain: ActivityMainBinding
    private lateinit var musicManager: MusicManager
    private var isLocalMode: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Inicializar MusicManager
        musicManager = MusicManager.getInstance(this)

        // Ir directamente al layout principal (la presentación ya se hizo en PresentationActivity)
        bindingMain = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindingMain.root)

        initMainLayout()

    }

    private fun initMainLayout() {
        val drawerLayout = bindingMain.drawerLayout
        val navigationView = bindingMain.navigationView
        val toolbar = bindingMain.toolbar

        // Determinar el modo de juego
        isLocalMode = intent.getBooleanExtra("MODE_LOCAL", !AuthManager.getInstance(this).isUserLoggedIn())

        setSupportActionBar(toolbar)

        // Iniciar música de fondo
        musicManager.startBackgroundMusic()

        val toggle = androidx.appcompat.app.ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.open, R.string.close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Configurar visibilidad de los elementos del menú según el modo
        configurarMenuDinamico(navigationView.menu)

        // Cargar el fragmento por defecto según el modo
        if (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) == null) {
            val fragmentoInicial = if (isLocalMode) JuegoFragment() else JuegoOnlineFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragmentoInicial)
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
                R.id.nav_online -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, JuegoOnlineFragment())
                        .addToBackStack(null)
                        .commit()
                }
                R.id.nav_ranking -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, RankingFragment())
                        .addToBackStack(null)
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
                R.id.nav_settings -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, SettingsFragment())
                        .addToBackStack(null)
                        .commit()
                }
                R.id.nav_sign_out -> {
                    // Cerrar sesión mediante AuthManager y volver a LoginActivity (Modo Online)
                    lifecycleScope.launch {
                        AuthManager.getInstance(this@MainActivity).signOut(this@MainActivity)
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
                R.id.nav_sign_in -> {
                    // Ir a LoginActivity para iniciar sesión (Modo Local)
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun configurarMenuDinamico(menu: android.view.Menu) {
        // Modo Online: muestra JuegoOnline, Ranking y Sign Out (nav_sign_out)
        menu.findItem(R.id.nav_online).isVisible = !isLocalMode
        menu.findItem(R.id.nav_ranking).isVisible = !isLocalMode
        menu.findItem(R.id.nav_sign_out).isVisible = !isLocalMode

        // Modo Local: muestra Juego, History y Sign In (nav_sign_in)
        menu.findItem(R.id.nav_home).isVisible = isLocalMode
        menu.findItem(R.id.nav_history).isVisible = isLocalMode
        menu.findItem(R.id.nav_sign_in).isVisible = isLocalMode
        
        // Comunes: Help, Music Selector, Settings (Siempre visibles por defecto)
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
