package br.com.sentinellock

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class LerQrcodeActivity : AppCompatActivity() {

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ler_qrcode)

        // Inicia o scanner de QR code
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE) // Define para ler apenas QR codes
        integrator.setPrompt("Aponte a câmera para o QR Code") // Mensagem para o usuário
        integrator.setCameraId(0) // Use a câmera traseira
        integrator.setBeepEnabled(false) // Habilita som ao ler QR code
        integrator.setBarcodeImageEnabled(true) // Salva uma imagem do QR code lido
        integrator.initiateScan()
    }



    // Método que lida com o resultado do scanner de QR code
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        result?.let { res ->
            if (res.contents != null) {
                val qrDataParts = res.contents.split(",")

                // Verifica se o conteúdo do QR code pode ser dividido corretamente
                if (qrDataParts.size >= 4) {
                    val userId = qrDataParts[0].substringAfter(":").trim()
                    val lockerId = qrDataParts[1].substringAfter(":").trim()
                    val price = qrDataParts[2].substringAfter(":").trim().toDoubleOrNull()
                    val duration = qrDataParts[3].substringAfter(":").trim().toLongOrNull()

                    if (userId != null && lockerId != null && price != null && duration != null) {
                        // Verifica a disponibilidade do armário antes de continuar
                        checkLockerAvailability(userId, lockerId, price, duration)
                    } else {
                        Toast.makeText(this, "QR code inválido,${userId},${lockerId},${price},${duration}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "QR code incompleto", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Falha na leitura do QR code", Toast.LENGTH_LONG).show()
            }
        }
    }



    private fun checkLockerAvailability(userId: String, lockerId: String, price: Double, duration: Long) {
        val lockerRef = db.collection("unidade_de_locacao").document(lockerId)
        lockerRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val isAvailable = document.getBoolean("status") ?: false
                if (isAvailable) {
                    updateUserBalanceAndNavigate(userId, lockerId, price, duration)
                } else {
                    showLockerUnavailableDialog()
                }
            } else {
                Toast.makeText(this, "Armário não encontrado", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Erro ao verificar o status do armário", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLockerUnavailableDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Armário Indisponível")
        builder.setMessage("Armário indisponível, avise ao cliente para escolher outro armário.")
        builder.setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
            val intent = Intent(this, MenuGerente::class.java)
            startActivity(intent)
            finish()
        }
        builder.show()
    }

    private fun updateUserBalanceAndNavigate(userId: String, lockerId: String, price: Double, duration: Long) {
        updateUserBalance(userId, price) { success ->
            if (success) {
                updateLockerStatus(lockerId) { success ->
                    if (success) {
                        Toast.makeText(this, "Operações realizadas com sucesso", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, QuantidadePessoas::class.java)
                        intent.putExtra("userId", userId)
                        intent.putExtra("lockerId", lockerId)
                        intent.putExtra("price", price)
                        intent.putExtra("duration", duration)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Erro ao atualizar o status do armário", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Erro ao atualizar o saldo do usuário", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUserBalance(userId: String, price: Double, onComplete: (Boolean) -> Unit) {
        val userRef = db.collection("pessoas").document(userId)

        db.runTransaction { transaction ->
            val userDoc = transaction.get(userRef)
            val currentBalance = userDoc.getDouble("cartaoCredito.saldo") ?: 0.0
            val updatedBalance = currentBalance - price
            transaction.update(userRef, "cartaoCredito.saldo", updatedBalance)
        }.addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }



    private fun updateLockerStatus(lockerId: String, onComplete: (Boolean) -> Unit) {
        val lockerRef = db.collection("unidade_de_locacao").document(lockerId)

        lockerRef.update("status", false, "aberto", false)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }
}
