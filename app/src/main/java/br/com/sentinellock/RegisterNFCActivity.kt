package br.com.sentinellock

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class RegisterNFCActivity : AppCompatActivity() {

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var imageFilePaths: List<String>
    private lateinit var userId: String
    private lateinit var lockerId: String
    private var price: Double = 0.0
    private var duration: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_nfcactivity)

        // Recebendo os dados da tela anterior
        imageFilePaths = intent.getStringArrayListExtra("IMAGE_PATHS") ?: emptyList()
        userId = intent.getStringExtra("userId") ?: ""
        lockerId = intent.getStringExtra("lockerId") ?: ""
        price = intent.getDoubleExtra("price", 0.0)
        duration = intent.getLongExtra("duration", 0)

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
            NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            if (tag != null) {
                writeNfcTag(tag)
            }
        }
    }

    private fun writeNfcTag(tag: Tag) {
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            try {
                ndef.connect()
                if (!ndef.isWritable) {
                    Toast.makeText(this, "Tag NFC não é gravável.", Toast.LENGTH_LONG).show()
                    return
                }

                val records = mutableListOf<NdefRecord>()
                // Adiciona os registros de texto para as informações existentes e para os caminhos das imagens
                records.addAll(imageFilePaths.map { filePath ->
                    NdefRecord.createTextRecord(null, filePath)
                })
                // Adiciona os registros de texto para os dados extras
                records.add(NdefRecord.createTextRecord(null, "userId: $userId"))
                records.add(NdefRecord.createTextRecord(null, "lockerId: $lockerId"))
                records.add(NdefRecord.createTextRecord(null, "price: $price"))
                records.add(NdefRecord.createTextRecord(null, "duration: $duration"))

                val message = NdefMessage(records.toTypedArray())

                ndef.writeNdefMessage(message)
                Toast.makeText(this, "Cadastro feito com sucesso", Toast.LENGTH_LONG).show()

                // Após a gravação na NFC, inicia a próxima atividade e passa os dados como extras
                val nextIntent = Intent(this, LiberadoInfosActivity2::class.java).apply {
                    putExtra("userId", userId)
                    putExtra("lockerId", lockerId)
                    putExtra("price", price)
                    putExtra("duration", duration)
                    putStringArrayListExtra("IMAGE_PATHS", ArrayList(imageFilePaths))  // Passa os caminhos das imagens
                }
                startActivity(nextIntent)
                // Finaliza esta atividade, se desejar
                finish()
            } catch (e: Exception) {
                Toast.makeText(this, "Erro ao escrever na tag: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                ndef.close()
            }
        }
    }
}
