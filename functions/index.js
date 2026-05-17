const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.procesarApuesta = functions.https.onRequest(async (req, res) => {
    if (req.method !== 'POST') return res.status(405).send('Method Not Allowed');

    const authorization = req.get('Authorization');
    if (!authorization || !authorization.startsWith('Bearer ')) {
        return res.status(401).json({ success: false, error: 'No autorizado' });
    }

    const idToken = authorization.split('Bearer ')[1];

    try {
        const decodedToken = await admin.auth().verifyIdToken(idToken);
        const authUserId = decodedToken.uid;
        const { apuesta, eleccionCara, userId } = req.body;

        if (userId === undefined || apuesta === undefined || eleccionCara === undefined) {
            return res.status(400).json({ success: false, error: 'Faltan parámetros en la petición' });
        }

        if (userId !== authUserId) {
            return res.status(403).json({ success: false, error: 'Intento de suplantación' });
        }

        const db = admin.firestore();
        const userRef = db.collection('usuarios').doc(authUserId);
        const globalRef = db.collection('config').doc('global');
        const rankingRef = db.collection('ranking');

        const resultado = await db.runTransaction(async (t) => {
            const userSnap = await t.get(userRef);
            const globalSnap = await t.get(globalRef);
            const rankingSnap = await t.get(rankingRef.orderBy('premio', 'desc').limit(10));

            if (!userSnap.exists) throw new Error('Usuario no encontrado');

            const userData = userSnap.data();
            const globalData = globalSnap.data() || { boteComun: 0 };

            const saldoActual = userData.saldo || 0;
            const boteActual = globalData.boteComun || 0;

            if (saldoActual < apuesta) throw new Error('Saldo insuficiente');

            const resultadoAzar = Math.random() < 0.5 ? 'Cara' : 'Cruz';
            const gano = (eleccionCara && resultadoAzar === 'Cara') || (!eleccionCara && resultadoAzar === 'Cruz');

            const boteConApuesta = boteActual + apuesta;
            const saldoTrasApuesta = saldoActual - apuesta;

            let nuevoSaldo, nuevoBote, premio = 0;

            if (gano) {
                premio = boteConApuesta;
                nuevoSaldo = saldoTrasApuesta + premio;
                nuevoBote = 0;
            } else {
                nuevoSaldo = saldoTrasApuesta;
                nuevoBote = boteConApuesta;
            }

            // 1. Actualizar saldos
            t.update(userRef, { saldo: nuevoSaldo });
            t.set(globalRef, { boteComun: nuevoBote }, { merge: true });

            // 2. Registrar partida
            const nuevaPartidaRef = userRef.collection('partidas').doc();
            t.set(nuevaPartidaRef, {
                apuesta: apuesta,
                resultado: resultadoAzar,
                gano: gano,
                premioObtenido: premio,
                fecha: admin.firestore.FieldValue.serverTimestamp()
            });

            // 3. Lógica de Ranking (Solo si gana)
            if (gano && premio > 0) {
                const rankingDocs = rankingSnap.docs;

                let entraEnRanking = false;
                if (rankingDocs.length < 10) {
                    entraEnRanking = true;
                } else {
                    const ultimoPremio = rankingDocs[rankingDocs.length - 1].data().premio;
                    if (premio > ultimoPremio) entraEnRanking = true;
                }

                if (entraEnRanking) {
                    const newRecordRef = rankingRef.doc();
                    t.set(newRecordRef, {
                        nombreUsuario: decodedToken.name || decodedToken.email || "Jugador Anónimo",
                        premio: premio,
                        fecha: Date.now()
                    });

                    // Si ahora tenemos 11 (10 previos + 1 nuevo), borramos el sobrante
                    if (rankingDocs.length >= 10) {
                        t.delete(rankingDocs[rankingDocs.length - 1].ref);
                    }
                }
            }

            return {
                success: true,
                resultado: resultadoAzar,
                gano: gano,
                nuevoSaldo: nuevoSaldo,
                nuevoBote: nuevoBote,
                premio: premio
            };
        });

        res.status(200).json(resultado);
    } catch (error) {
        console.error('Error Transaction:', error.message);
        res.status(400).json({ success: false, error: error.message });
    }
});

exports.getRanking = functions.https.onRequest(async (req, res) => {
    try {
        const snapshot = await admin.firestore().collection('ranking')
            .orderBy('premio', 'desc')
            .limit(10)
            .get();
        const ranking = snapshot.docs.map(doc => doc.data());
        res.status(200).json(ranking);
    } catch (error) {
        res.status(500).send('Error');
    }
});