package com.example.caraocruz

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.caraocruz.databinding.ActivityMainBinding
import com.example.caraocruz.databinding.ActivityPresentationBinding

class MainActivity : AppCompatActivity() {

    private lateinit var bindingMain: ActivityMainBinding
    private lateinit var bindingPresentation: ActivityPresentationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cargar primero el layout de presentación
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

        val toggle = androidx.appcompat.app.ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.open, R.string.close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> { /* Acción jugar */ }
                R.id.nav_ranking -> { /* Acción ranking */ }
                R.id.nav_profile -> { /* Acción perfil */ }
                R.id.nav_settings -> { /* Acción configuración */ }
            }
            drawerLayout.closeDrawers()
            true
        }
    }
}
