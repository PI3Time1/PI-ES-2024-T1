package br.com.sentinellock

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*

class EncerrarReadNfcActivity : AppCompatActivity() {

    private lateinit var nfcAdapter: NfcAdapter
    private var leituraFeita = 0
    private var numeroDeLeituras = 1
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encerrar_read_nfc)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "Este dispositivo não suporta NFC.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        if (!nfcAdapter.isEnabled) {
            Toast.makeText(this, "Ative o NFC nas configurações.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        val filters = arrayOf(
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        )
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == intent.action
        ) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            if (tag != null) {
                readNfcTag(tag)
            }
        }
    }

    private fun readNfcTag(tag: Tag) {
        val ndef = Ndef.get(tag)
        try {
            ndef.connect()
            val ndefMessage = ndef.ndefMessage

            if (ndefMessage != null) {
                val records = ndefMessage.records
                if (records.isNotEmpty()) {
                    var lockerId: String
                    var horaCelular: String

                    if (records.size >= 7) {
                        lockerId = records[3].payload.decodeToString().trim { it <= ' ' }.substringAfter("lockerId: ")
                        horaCelular = records[6].payload.decodeToString().trim { it <= ' ' }.substringAfter("current_time: ")
                        numeroDeLeituras = 2
                    } else if (records.size >= 6){
                        lockerId = records[2].payload.decodeToString().trim { it <= ' ' }.substringAfter("lockerId: ")
                        horaCelular = records[5].payload.decodeToString().trim { it <= ' ' }.substringAfter("current_time: ")
                        numeroDeLeituras = 1
                    }else{
                        showEmptyTagDialog()
                        return
                    }

                    Log.d("NFC_TAG", "Locker ID: $lockerId, Hora Celular: $horaCelular")

                    leituraFeita++
                    if (leituraFeita >= numeroDeLeituras) {
                        displayLockerInfo(lockerId, horaCelular)
                    } else {
                        Toast.makeText(this, "Aproxime a próxima tag NFC.", Toast.LENGTH_LONG).show()
                    }
                }
            }else{
                showEmptyTagDialog()
            }

            ndef.close()
            clearNfcTag(tag)
        } catch (e: IOException) {
            showEmptyTagDialog()
            Log.e("NFC_TAG", "Erro ao conectar à tag NFC", e)
        } finally {
            try {
                ndef.close()
            } catch (e: IOException) {
                Log.e("NFC_TAG", "Erro ao fechar a conexão com a tag NFC", e)
            }
        }
    }

    private fun showEmptyTagDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Tag vazia, não há nada para ler.")
            .setPositiveButton("Fechar") { dialog, _ ->
                val intent = Intent(this, MenuGerente::class.java)
                startActivity(intent)
                finish()
            }
        val dialog = builder.create()
        dialog.show()
    }

    private fun clearNfcTag(tag: Tag) {
        try {
            val ndef = Ndef.get(tag)
            ndef.connect()
            val emptyRecord = NdefRecord(NdefRecord.TNF_EMPTY, null, null, null)
            val emptyNdefMessage = NdefMessage(arrayOf(emptyRecord))
            ndef.writeNdefMessage(emptyNdefMessage)
            Toast.makeText(this, "Tag NFC limpa com sucesso.", Toast.LENGTH_SHORT).show()
            ndef.close()
        } catch (e: Exception) {
            Log.e("NFC_TAG", "Erro ao limpar a tag NFC", e)
            Toast.makeText(this, "Erro ao limpar a tag NFC.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayLockerInfo(lockerId: String?, horaCelular: String) {
        if (lockerId != null) {
            GlobalScope.launch(Dispatchers.Main) {
                try {
                    val valorDiaria: Double? = getDailyRate(lockerId)
                    val horaFim = Calendar.getInstance().time
                    val valorTempoUso = calculateUsageValue(horaCelular, horaFim, valorDiaria)
                    val valorEstorno = valorDiaria?.let { it - (valorTempoUso ?: 0.0) }
                    updateLeaseStatus(lockerId)
                    updateLeasePromotion(lockerId, valorEstorno)
                    goToLeaseEndActivity()
                } catch (e: Exception) {
                    e.printStackTrace()
                    showErrorMessage("Erro ao processar os dados.")
                }
            }
        } else {
            showErrorMessage("ID do armário não encontrado")
        }
    }

    private suspend fun getDailyRate(lockerId: String): Double? {
        return try {
            val document = db.collection("unidade_de_locacao").document(lockerId).get().await()
            document.getDouble("promocao")
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateUsageValue(startTime: String, endTime: Date, dailyRate: Double?): Double? {
        val formatoHora = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        // Obtém a data atual
        val dataAtual = formatoHora.format(Calendar.getInstance().time)

        // Parseia as datas
        val horaInicio = formatoHora.parse(startTime)
        val horaFim = endTime

        // Calcula a diferença de tempo em minutos
        val diferencaTempoMillis = horaFim.time - horaInicio.time
        val diferencaTempoMinutos = diferencaTempoMillis / (1000 * 60) // Convertendo de milissegundos para minutos

        // Descobre valor por hora para descobrir valor por minuto de uso
        val valorPorHora = dailyRate?.div(11.0) // 11 é o tempo total da diária (7h-18h)
        val valorPorMinuto = valorPorHora?.div(60.0)

        // Descobre quanto ficou a locação de acordo com o tempo de uso
        return valorPorMinuto?.let {
            val valor = diferencaTempoMinutos * it
            BigDecimal(valor).setScale(2, RoundingMode.HALF_UP).toDouble()
        }
    }

    private fun updateLeaseStatus(lockerId: String) {
        db.collection("unidade_de_locacao").document(lockerId)
            .update("status", true, "aberto", true)
            .addOnSuccessListener {
                Log.d("TAG", "Campos status e aberto atualizados com sucesso")
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Erro ao atualizar campos status e aberto", e)
            }
    }

    private fun updateLeasePromotion(lockerId: String, value: Double?) {
        val locacaoRef = db.collection("unidade_de_locacao").document(lockerId)

        value?.let { valorEstorno ->
            locacaoRef.update("cacao", FieldValue.increment(valorEstorno))
                .addOnSuccessListener {
                    Log.d("TAG", "Campo cacao atualizado com sucesso")
                }
                .addOnFailureListener { e ->
                    Log.e("TAG", "Erro ao atualizar campo cacao", e)
                }
        }
    }

    private fun goToLeaseEndActivity() {
        val intent = Intent(this@EncerrarReadNfcActivity, locacao_encerrada::class.java)
        startActivity(intent)
        finish() // Opcional: encerra a atividade atual se necessário
    }

    private fun showErrorMessage(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}

