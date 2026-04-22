package com.example.caraocruz.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "tabla_historico")
data class Partida(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val apuesta: Int,
    val resultado: String,
    val gano: Boolean,
    val fecha: Date,
    val latitud: Double? = null,
    val longitud: Double? = null
)