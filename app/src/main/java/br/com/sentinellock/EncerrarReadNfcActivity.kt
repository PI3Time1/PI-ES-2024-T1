package br.com.sentinellock

import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.util.Locale

class EncerrarReadNfcActivity : AppCompatActivity() {

    private lateinit var nfcAdapter: NfcAdapter


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
        var userId: Any
        var lockerId: Any
        var duration: Any
        var price: Any
        var horaCelular: Any

        val ndef = Ndef.get(tag)
        if (ndef != null) {
            ndef.connect()
            val ndefMessage = ndef.ndefMessage


            if (ndefMessage != null) {
                val records = ndefMessage.records
                if (records.isNotEmpty()) {

                    if (records.size >= 8) {

                        lockerId = records[3].payload.decodeToString()
                        duration = records[5].payload.decodeToString()
                        price = records[4].payload.decodeToString()

                        horaCelular = records[6].payload.decodeToString().trim()
                    } else {

                        lockerId = records[2].payload.decodeToString()
                        duration = records[4].payload.decodeToString()
                        price = records[3].payload.decodeToString()

                        horaCelular = records[5].payload.decodeToString().trim()
                    }


                    Log.d("NFC_TAG", "User ID: passo1 $records")

                    // Logging das informações lidas da tag

                    price = price.trim { it <= ' ' }.substringAfter("price: ")
                    duration = duration.trim { it <= ' ' }.substringAfter("duration: ")

                    lockerId = lockerId.trim { it <= ' ' }.substringAfter("lockerId: ")
                    horaCelular = horaCelular.trim { it <= ' ' }.substringAfter("current_time: ")



                    displayLockerInfo(lockerId, horaCelular)


                }
            }
            ndef.close()
        }
    }


    private fun displayLockerInfo(lockerId: String?, horaCelular: Any) {

        if (lockerId != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("unidade_de_locacao").document(lockerId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val lockerName = document.getString("name")

                        // Define os campos status e aberto como falsos
                        db.collection("unidade_de_locacao").document(lockerId)
                            .update("status", false, "aberto", false)
                            .addOnSuccessListener {
                                Log.d("TAG", "Campos status e aberto atualizados com sucesso")

                                // Navega para a tela LocacaoEncerradaActivity
                                val intent = Intent(this, locacao_encerrada::class.java)
                                startActivity(intent)
                                finish() // Opcional: encerra a atividade atual se necessário
                            }
                            .addOnFailureListener { e ->
                                Log.w("TAG", "Erro ao atualizar campos status e aberto", e)
                            }

                    } else {
                        Toast.makeText(
                            this,
                            "Armário não encontrado no Firebase",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Erro ao buscar informações do armário: $e",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            Toast.makeText(this, "ID do armário não encontrado", Toast.LENGTH_SHORT).show()
        }
    }
}



