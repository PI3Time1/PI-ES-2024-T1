package br.com.sentinellock

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.nfc.NfcAdapter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class ReadNFCActivity : AppCompatActivity() {

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var tvTagContent: TextView
    private lateinit var imgTagImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_nfcactivity)

        tvTagContent = findViewById(R.id.tv_tag_content)
        imgTagImage = findViewById(R.id.img_tag_image)

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

                    // Carregando e exibindo uma imagem
                    loadImageFromPath(tagContent1)

                    // Limpando a tag
//                    clearNfcTag(ndef)
                }
            }
            ndef.close()
        }
    }

    private fun loadImageFromPath(imagePath: String) {
        try {
            // Removendo a parte indesejada antes de /storage
            val pathPrefix = "/storage"
            val startIndex = imagePath.indexOf(pathPrefix)
            val trimmedPath = if (startIndex != -1) {
                imagePath.substring(startIndex)
            } else {
                imagePath
            }

            // Verificar se o caminho absoluto é válido
            val file = File(trimmedPath)
            Log.d("LoadImage", "Imagem carregada com sucesso: $file")

            if (file.exists() && file.canRead()) {
                val bitmap = BitmapFactory.decodeFile(trimmedPath)
                if (bitmap != null) {
                    imgTagImage.setImageBitmap(bitmap)
                    imgTagImage.visibility = View.VISIBLE // tornar o ImageView visível
                    Log.d("LoadImage", "Imagem carregada com sucesso: $bitmap")
                    Log.d("LoadImage", "Imagem carregada com sucesso: $imgTagImage")
                    Log.d("LoadImage", "Imagem carregada com sucesso: $file")
                } else {
                    Toast.makeText(this, "Falha ao decodificar o bitmap da imagem.", Toast.LENGTH_LONG).show()
                    Log.e("LoadImage", "Falha ao decodificar o bitmap da imagem: $trimmedPath")
                }
            } else {
                Toast.makeText(this, "O arquivo de imagem não existe ou não pode ser lido.", Toast.LENGTH_LONG).show()
                Log.e("LoadImage", "O arquivo de imagem não existe ou não pode ser lido: $trimmedPath")
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao carregar a imagem: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("LoadImage", "Erro ao carregar a imagem: ${e.message}")
        }
    }









    private fun clearNfcTag(ndef: Ndef) {
        try {
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
