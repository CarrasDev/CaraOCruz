package com.example.caraocruz.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.caraocruz.data.AppDatabase
import com.example.caraocruz.data.Partida
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class HistoryViewModel(private val database: AppDatabase) : ViewModel() {

    fun getAllPartidas(): Single<List<Partida>> {
        return database.juegoDao()
            .getAllPartidas()
            .subscribeOn(Schedulers.io())
    }

    fun getMejoresPuntuaciones(): Single<List<Partida>> {
        return database.juegoDao()
            .getMejoresPuntuaciones()
            .subscribeOn(Schedulers.io())
    }
}

class HistoryViewModelFactory(
    private val database: AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
