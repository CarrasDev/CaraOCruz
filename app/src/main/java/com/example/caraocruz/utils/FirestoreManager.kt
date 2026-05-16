package com.example.caraocruz.utils

import android.util.Log
import com.example.caraocruz.data.Partida
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreManager private constructor() {

    private val db = FirebaseFirestore.getInstance()
    private val usuariosCollection = db.collection("usuarios")

    companion object {
        @Volatile
        private var INSTANCE: FirestoreManager? = null

        fun getInstance(): FirestoreManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirestoreManager().also { INSTANCE = it }
            }
        }
    }

    /**
     * Inicializa el perfil del usuario si no existe, o recupera el saldo actual.
     * Si es un usuario nuevo, le damos 100 monedas de cortesía.
     */
    suspend fun getOrInitializeUser(userId: String): Long {
        return try {
            val document = usuariosCollection.document(userId).get().await()
            if (document.exists()) {
                document.getLong("saldo") ?: 0L
            } else {
                val initialData = mapOf("saldo" to 100L)
                usuariosCollection.document(userId).set(initialData).await()
                100L
            }
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Error al obtener/inicializar usuario", e)
            0L
        }
    }

    /**
     * Procesa una jugada de forma atómica usando una Transacción.
     * Esto asegura que el saldo se actualice correctamente incluso con mala conexión.
     */
    suspend fun procesarJugada(userId: String, apuesta: Int, gano: Boolean, partida: Partida): Result<Long> {
        return try {
            val userRef = usuariosCollection.document(userId)
            val partidasRef = userRef.collection("partidas")

            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val saldoActual = snapshot.getLong("saldo") ?: 0L
                
                val nuevoSaldo = if (gano) {
                    saldoActual + apuesta
                } else {
                    saldoActual - apuesta
                }

                if (nuevoSaldo < 0) throw Exception("Saldo insuficiente")

                // 1. Actualizar saldo del usuario
                transaction.update(userRef, "saldo", nuevoSaldo)
                
                // 2. Registrar la partida en la subcolección
                // Convertimos el objeto Partida a un mapa para Firestore
                val partidaMap = mapOf(
                    "resultado" to partida.resultado,
                    "apuesta" to partida.apuesta,
                    "gano" to partida.gano,
                    "fecha" to partida.fecha,
                    "latitud" to partida.latitud,
                    "longitud" to partida.longitud
                )
                partidasRef.add(partidaMap)

                nuevoSaldo
            }.await()

            val finalSaldo = getOrInitializeUser(userId) // Opcional: podrías devolver el resultado de la transacción
            Result.success(finalSaldo)
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Error en transacción de jugada", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene el flujo de las últimas partidas del usuario (para el historial online)
     */
    suspend fun getHistorialOnline(userId: String) = try {
        usuariosCollection.document(userId)
            .collection("partidas")
            .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .await()
    } catch (e: Exception) {
        null
    }
}
