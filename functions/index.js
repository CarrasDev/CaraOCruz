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

        if (userId !== authUserId) {
            return res.status(403).json({ success: false, error: 'Intento de suplantación' });
        }

        const db = admin.firestore();
        const userRef = db.collection('usuarios').doc(authUserId);
        const globalRef = db.collection('config').doc('global');
        const partidasRef = userRef.collection('partidas'); // Referencia a la subcolección

        const resultado = await db.runTransaction(async (t) => {
            const userSnap = await t.get(userRef);
            const globalSnap = await t.get(globalRef);

            if (!userSnap.exists) throw new Error('Usuario no encontrado');

            const saldoActual = userSnap.data().saldo || 0;
            const boteActual = globalSnap.data().boteComun || 0;

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

            // Actualizar saldos globales
            t.update(userRef, { saldo: nuevoSaldo });
            t.update(globalRef, { boteComun: nuevoBote });

            // REGISTRAR LA PARTIDA
            const nuevaPartidaRef = partidasRef.doc(); // Crea un ID automático
            t.set(nuevaPartidaRef, {
                apuesta: apuesta,
                resultado: resultadoAzar,
                gano: gano,
                premioObtenido: premio,
                fecha: admin.firestore.FieldValue.serverTimestamp() // Fecha del servidor
            });

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
        console.error('Error:', error);
        res.status(400).json({ success: false, error: error.message });
    }
});