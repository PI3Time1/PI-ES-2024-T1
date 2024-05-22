package br.com.sentinellock

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class RegisterNFCActivity : AppCompatActivity() {

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var imageFilePaths: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_nfcactivity)

        imageFilePaths = intent.getStringArrayListExtra("IMAGE_PATHS") ?: emptyList()

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "Este dispositivo não suporta NFC.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        if (!nfcAdapter.isEnabled) {
            Toast.makeText(this, "Ative o NFC nas configurações.", Toast.LENGTH_LONG).show()
        }

        val btnNext: Button = findViewById(R.id.btn_next)
        btnNext.setOnClickListener {
            val intent = Intent(this, ReadNFCActivity::class.java)
            startActivity(intent)
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
                writeNfcTag(tag, imageFilePaths)
            }
        }
    }

    private fun writeNfcTag(tag: Tag, imageFilePaths: List<String>) {
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            try {
                ndef.connect()
                if (!ndef.isWritable) {
                    Toast.makeText(this, "Tag NFC não é gravável.", Toast.LENGTH_LONG).show()
                    return
                }

                val records = imageFilePaths.map { filePath ->
                    NdefRecord.createTextRecord(null, filePath)
                }.toTypedArray()

                val message = NdefMessage(records)

                ndef.writeNdefMessage(message)
                Toast.makeText(this, "Cadastro feito com sucesso", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Erro ao escrever na tag: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                ndef.close()
            }
        } else {
            val ndefFormatable = NdefFormatable.get(tag)
            if (ndefFormatable != null) {
                try {
                    ndefFormatable.connect()

                    val records = imageFilePaths.map { filePath ->
                        NdefRecord.createTextRecord(null, filePath)
                    }.toTypedArray()

                    val message = NdefMessage(records)

                    ndefFormatable.format(message)
                    Toast.makeText(this, "Cadastro feito com sucesso", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao formatar a tag: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    ndefFormatable.close()
                }
            } else {
                Toast.makeText(this, "Tag NFC não suporta NDEF.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
