const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.procesarApuesta = functions.https.onRequest(async (req, res) => {
    // 1. Validar método y Auth
    if (req.method !== 'POST') return res.status(405).send('Method Not Allowed');
    
    // Aquí deberías validar el ID Token que enviamos en la cabecera "Authorization"
    // Por simplicidad en este ejemplo, extraemos el userId del body
    const { userId, apuesta, eleccionCara } = req.body;

    const db = admin.firestore();
    const userRef = db.collection('usuarios').doc(userId);
    const globalRef = db.collection('config').doc('global');

    try {
        const resultado = await db.runTransaction(async (t) => {
            const userSnap = await t.get(userRef);
            const globalSnap = await t.get(globalRef);

            const saldoActual = userSnap.data().saldo || 0;
            const boteActual = globalSnap.data().boteComun || 0;

            if (saldoActual < apuesta) throw new Error('Saldo insuficiente');

            // Lógica de Azar en el Servidor
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

            // Actualizar Firestore
            t.update(userRef, { saldo: nuevoSaldo });
            t.update(globalRef, { boteComun: nuevoBote });

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
        res.status(400).json({ success: false, error: error.message });
    }
});