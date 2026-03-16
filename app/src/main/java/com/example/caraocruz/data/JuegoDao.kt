package com.example.caraocruz.data

import androidx.room.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

@Dao
interface JuegoDao {
    @Query("SELECT * FROM tabla_usuario WHERE id = 1")
    fun getUsuario(): Flowable<Usuario>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun guardarUsuario(usuario: Usuario): Completable

    @Insert
    fun insertarPartida(partida: Partida): Completable

    @Query("SELECT * FROM tabla_historico WHERE gano = 1 ORDER BY apuesta DESC")
    fun getMejoresPuntuaciones(): Single<List<Partida>>
}