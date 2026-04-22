package com.example.caraocruz.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.caraocruz.data.JuegoRepository
import com.example.caraocruz.data.Partida
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class HistoryViewModel(private val repository: JuegoRepository) : ViewModel() {

    fun getAllPartidas(): Single<List<Partida>> {
        return repository.getAllPartidas()
    }

    fun getMejoresPuntuaciones(): Single<List<Partida>> {
        return repository.getRanking()
    }
}

class HistoryViewModelFactory(
    private val repository: JuegoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
