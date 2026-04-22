package com.example.caraocruz.data

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class JuegoRepository(private val juegoDao: JuegoDao) {

    fun getUsuario(): Flowable<Usuario> {
        return juegoDao.getUsuario()
            .subscribeOn(Schedulers.io())
    }

    fun guardarUsuario(usuario: Usuario): Completable {
        return juegoDao.guardarUsuario(usuario)
            .subscribeOn(Schedulers.io())
    }

    fun insertarPartida(partida: Partida): Completable {
        return juegoDao.insertarPartida(partida)
            .subscribeOn(Schedulers.io())
    }

    fun getRanking(): Single<List<Partida>> {
        return juegoDao.getMejoresPuntuaciones()
            .subscribeOn(Schedulers.io())
    }

    fun getAllPartidas(): Single<List<Partida>> {
        return juegoDao.getAllPartidas()
            .subscribeOn(Schedulers.io())
    }
}