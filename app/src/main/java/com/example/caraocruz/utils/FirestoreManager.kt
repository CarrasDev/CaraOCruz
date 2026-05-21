package com.example.caraocruz.utils

import android.util.Log
import com.example.caraocruz.data.Partida
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreManager private constructor() {

    private val db = FirebaseFirestore.getInstance()
    private val usuariosCollection = db.collection("usuarios")
    private val globalConfigRef = db.collection("config").document("global")

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
     * Escucha cambios en el bote común en tiempo real.
     */
    fun getBoteComunListener(onUpdate: (Long) -> Unit) {
        globalConfigRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("FirestoreManager", "Error al escuchar bote común", e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val bote = snapshot.getLong("boteComun") ?: 0L
                onUpdate(bote)
            } else {
                // Si no existe, lo inicializamos
                globalConfigRef.set(mapOf("boteComun" to 0L))
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
     * Gestiona el Bote Común:
     * - Si pierde: Su apuesta se suma al Bote Común.
     * - Si gana: Se lleva el saldo actual del Bote Común y el bote se resetea.
     */
    suspend fun procesarJugadaOnline(userId: String, apuesta: Int, gano: Boolean, partida: Partida): Result<Pair<Long, Long>> {
        return try {
            val userRef = usuariosCollection.document(userId)
            val partidasRef = userRef.collection("partidas")

            db.runTransaction { transaction ->
                // 1. Obtener datos actuales
                val userSnapshot = transaction.get(userRef)
                val globalSnapshot = transaction.get(globalConfigRef)
                
                val saldoActual = userSnapshot.getLong("saldo") ?: 0L
                val boteActual = globalSnapshot.getLong("boteComun") ?: 0L
                
                if (saldoActual < apuesta) throw Exception("Saldo insuficiente")

                val nuevoSaldo: Long
                val nuevoBote: Long
                val premioObtenido: Long

                if (gano) {
                    // El jugador gana todo lo que hay en el bote
                    premioObtenido = boteActual
                    nuevoSaldo = saldoActual + premioObtenido
                    nuevoBote = 0L // El bote se vacía
                } else {
                    // El jugador pierde su apuesta y se suma al bote
                    premioObtenido = 0L
                    nuevoSaldo = saldoActual - apuesta
                    nuevoBote = boteActual + apuesta
                }

                // 2. Aplicar cambios atómicamente
                transaction.update(userRef, "saldo", nuevoSaldo)
                transaction.update(globalConfigRef, "boteComun", nuevoBote)
                
                // 3. Registrar partida
                val partidaMap = mutableMapOf(
                    "resultado" to partida.resultado,
                    "apuesta" to partida.apuesta,
                    "gano" to gano,
                    "fecha" to partida.fecha,
                    "premioObtenido" to premioObtenido,
                    "latitud" to partida.latitud,
                    "longitud" to partida.longitud
                )
                partidasRef.add(partidaMap)

                Pair(nuevoSaldo, premioObtenido)
            }.await().let { 
                Result.success(it)
            }
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Error en transacción de jugada online", e)
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

    /**
     * Reinicia el saldo del usuario a 100 monedas en Firestore.
     */
    suspend fun reiniciarSaldo(userId: String): Result<Long> {
        return try {
            val userRef = usuariosCollection.document(userId)
            userRef.update("saldo", 100L).await()
            Result.success(100L)
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Error al reiniciar saldo", e)
            Result.failure(e)
        }
    }
}
