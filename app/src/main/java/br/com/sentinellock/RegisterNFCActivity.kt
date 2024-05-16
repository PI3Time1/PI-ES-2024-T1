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
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class RegisterNFCActivity : AppCompatActivity() {

    private lateinit var nfcAdapter: NfcAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_nfcactivity)

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
                writeNfcTag(tag, "teste", "teste2")
            }
        }
    }

    private fun writeNfcTag(tag: Tag, text1: String, text2: String) {
        val ndef = Ndef.get(tag) ?: return
        val message = NdefMessage(arrayOf(
            NdefRecord.createTextRecord(null, text1),
            NdefRecord.createTextRecord(null, text2)
        ))

        try {
            ndef.connect()
            ndef.writeNdefMessage(message)
            Toast.makeText(this, "Cadastro feito com sucesso", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao escrever na tag: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            ndef.close()
        }
    }
}
