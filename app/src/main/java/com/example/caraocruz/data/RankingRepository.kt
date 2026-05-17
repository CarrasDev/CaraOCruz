package com.example.caraocruz.data

import com.example.caraocruz.data.api.RetrofitClient
import com.example.caraocruz.data.api.RankingItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RankingRepository(private val dao: JuegoDao) {

    val rankingLocal: Flow<List<RankingItem>> = dao.getRankingLocal().map { entities ->
        entities.map { RankingItem(it.nombreUsuario, it.premio, it.fecha) }
    }

    suspend fun refreshRanking() {
        try {
            val remoteRanking = RetrofitClient.rankingInstance.obtenerRanking()
            val entities = remoteRanking.map { 
                RankingEntity(nombreUsuario = it.nombreUsuario, premio = it.premio, fecha = it.fecha) 
            }
            dao.borrarRanking()
            dao.guardarRanking(entities)
        } catch (e: Exception) {
            throw e
        }
    }
}
