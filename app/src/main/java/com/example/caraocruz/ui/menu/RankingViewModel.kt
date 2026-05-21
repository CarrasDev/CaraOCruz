package com.example.caraocruz.ui.menu

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.caraocruz.data.AppDatabase
import com.example.caraocruz.data.RankingRepository
import com.example.caraocruz.data.api.RankingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RankingState {
    object Loading : RankingState()
    data class Success(val ranking: List<RankingItem>) : RankingState()
    data class Error(val message: String) : RankingState()
}

class RankingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RankingRepository
    private val _state = MutableStateFlow<RankingState>(RankingState.Loading)
    val state: StateFlow<RankingState> = _state.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = RankingRepository(db.juegoDao())
        
        viewModelScope.launch {
            repository.rankingLocal.collect { localData ->
                if (localData.isNotEmpty()) {
                    _state.value = RankingState.Success(localData)
                }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            if (_state.value !is RankingState.Success) {
                _state.value = RankingState.Loading
            }
            try {
                repository.refreshRanking()
                // No necesitamos actualizar el estado aquí manualmente porque 
                // estamos recolectando de rankingLocal en el init
            } catch (e: Exception) {
                if (_state.value !is RankingState.Success) {
                    val errorMsg = getApplication<android.app.Application>().getString(com.example.caraocruz.R.string.error_loading_ranking)
                    _state.value = RankingState.Error(errorMsg)
                }
            }
        }
    }
}
