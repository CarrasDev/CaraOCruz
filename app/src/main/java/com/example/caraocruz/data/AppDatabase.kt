package com.example.caraocruz.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
@TypeConverters(Converters::class) //soluciona el error del missing type

// 1. Definimos qué tablas tiene la DB y qué versión es (importante para cambios futuros)
@Database(entities = [Usuario::class, Partida::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    // 2. Declaramos el DAO que creamos antes
    abstract fun juegoDao(): JuegoDao

    companion object {
        // 3. Creamos un Singleton (una única instancia para toda la app)
        // para evitar que se abran varias conexiones a la vez y se corrompan los datos.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cara_cruz_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}