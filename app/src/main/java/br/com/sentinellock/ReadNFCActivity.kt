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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ReadNFCActivity : AppCompatActivity() {

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var tvTagContent: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_nfcactivity)

        tvTagContent = findViewById(R.id.tv_tag_content)

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
                readNfcTag(tag)
            }
        }
    }

    private fun readNfcTag(tag: Tag) {
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            ndef.connect()
            val ndefMessage = ndef.ndefMessage
            if (ndefMessage != null) {
                val records = ndefMessage.records
                if (records.isNotEmpty()) {
                    val record1 = records[0]
                    val record2 = if (records.size > 1) records[1] else null
                    val text1 = record1.payload.decodeToString()
                    val text2 = record2?.payload?.decodeToString()

                    // Salvando os valores em variáveis
                    val tagContent1 = text1
                    val tagContent2 = text2 ?: ""

                    // Exibindo os valores na tela
                    tvTagContent.text = "Tag Content 1: $tagContent1\nTag Content 2: $tagContent2"

                    // Limpando a tag
                    clearNfcTag(ndef)
                }
            }
            ndef.close()
        }
    }

    private fun clearNfcTag(ndef: Ndef) {
        try {
            ndef.connect()
            val emptyMessage = NdefMessage(arrayOf(NdefRecord.createTextRecord(null, "")))
            ndef.writeNdefMessage(emptyMessage)
            Toast.makeText(this, "Tag limpa com sucesso", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao limpar a tag: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            ndef.close()
        }
    }
}
