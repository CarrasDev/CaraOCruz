package com.example.caraocruz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabla_usuario")
data class Usuario(
    @PrimaryKey val id: Int = 1,
    val monedas: Int
)