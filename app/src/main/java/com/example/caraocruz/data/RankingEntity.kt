package com.example.caraocruz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabla_ranking")
data class RankingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombreUsuario: String,
    val premio: Long,
    val fecha: Long
)
