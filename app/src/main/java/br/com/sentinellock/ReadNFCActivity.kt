package br.com.sentinellock

import android.app.AlertDialog
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
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class ReadNFCActivity : AppCompatActivity() {

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var tvUserInfo: TextView
    private lateinit var tvLockerInfo: TextView
    private lateinit var imageView1: ImageView
    private lateinit var imageView2: ImageView
    private lateinit var loadingDialog: Dialog
    private lateinit var buttonFecharArmario: Button
    private var infoLocker: String? = null
    private lateinit var button2: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_nfcactivity)
        button2 = findViewById(R.id.button2)

        tvUserInfo = findViewById(R.id.userInfoTextView)
        tvLockerInfo = findViewById(R.id.lockerInfoTextView)
        imageView1 = findViewById(R.id.imageView1)
        imageView2 = findViewById(R.id.imageView2)
        buttonFecharArmario = findViewById(R.id.button1)

        loadingDialog = Dialog(this)
        loadingDialog.setContentView(R.layout.dialog_loading)
        loadingDialog.setCancelable(false)
        loadingDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        button2.setOnClickListener {
            val intent = Intent(this, MenuGerente::class.java)
            infoLocker?.let { liberaArmario(it, 2) }
            startActivity(intent)
            finish()
        }

        loadingDialog.show()

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "Este dispositivo não suporta NFC.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        if (!nfcAdapter.isEnabled) {
            Toast.makeText(this, "Ative o NFC nas configurações.", Toast.LENGTH_LONG).show()
        }

        buttonFecharArmario.setOnClickListener {
            infoLocker?.let { liberaArmario(it, 1) }
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
        if (ndef != null) {

            ndef.connect()
            val ndefMessage = ndef.ndefMessage

            if (ndefMessage != null && ndefMessage.records.isNotEmpty()) {
                val records = ndefMessage.records
                var userId: String? = null
                var lockerId: String? = null
                var duration: String? = null
                var price: String? = null
                var imagePath1: String? = null
                var imagePath2: String? = null
                var horaCelular: String? = null

                try {
                    if (records.size >= 7) {
                        userId = records[2].payload.decodeToString()
                        lockerId = records[3].payload.decodeToString()
                        duration = records[5].payload.decodeToString()
                        price = records[4].payload.decodeToString()
                        imagePath1 = records[0].payload.decodeToString().trim()
                        imagePath2 = records[1].payload.decodeToString().trim()
                        horaCelular = records[6].payload.decodeToString().trim()
                        loadingDialog.dismiss()

                    } else if (records.size >= 6) {
                        userId = records[1].payload.decodeToString()
                        lockerId = records[2].payload.decodeToString()
                        duration = records[4].payload.decodeToString()
                        price = records[3].payload.decodeToString()
                        imagePath1 = records[0].payload.decodeToString().trim()
                        imagePath2 = null
                        horaCelular = records[5].payload.decodeToString().trim()
                        loadingDialog.dismiss()

                    } else {
                        showEmptyTagDialog()
                        return
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao ler os dados da tag: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("ReadNFC", "Erro ao ler os dados da tag", e)
                    showEmptyTagDialog()
                    return
                }

                price = price?.trim { it <= ' ' }?.substringAfter("price: ")
                duration = duration?.trim { it <= ' ' }?.substringAfter("duration: ")
                userId = userId?.trim { it <= ' ' }?.substringAfter("userId: ")
                lockerId = lockerId?.trim { it <= ' ' }?.substringAfter("lockerId: ")
                horaCelular = horaCelular?.trim { it <= ' ' }?.substringAfter("current_time: ")

                infoLocker = lockerId

                displayUserInfo(userId ?: "")
                displayLockerInfo(lockerId, price ?: "", duration ?: "", horaCelular ?: "")

                if (!imagePath1.isNullOrEmpty() || imagePath2 != null) {
                    if (imagePath2 != null) {
                        loadImageFromPath(this, imagePath1, imageView1)
                        loadImageFromPath(this, imagePath2, imageView2)
                        infoLocker?.let { liberaArmario(it, 3) }
                    } else {
                        loadImageFromPath(this, imagePath1, imageView1)
                        infoLocker?.let { liberaArmario(it, 3) }
                    }
                } else {
                    Toast.makeText(this, "Nenhuma imagem foi lida", Toast.LENGTH_SHORT).show()
                }
            } else {
                showEmptyTagDialog()
            }
            ndef.close()
        } else {
            showEmptyTagDialog()
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

    private fun displayUserInfo(userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("pessoas").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val userName = document.getString("nome")
                    val userPhone = document.getString("telefone")
                    val userCpf = document.getString("cpf")
                    val userInfo = "Nome: $userName    Telefone: $userPhone\nCPF: $userCpf"
                    tvUserInfo.text = userInfo
                } else {
                    Toast.makeText(this, "Usuário não encontrado no Firebase", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Erro ao buscar informações do usuário: $e",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun displayLockerInfo(lockerId: String?, price: String, duration: String, horaCelular: String) {
        if (lockerId != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("unidade_de_locacao").document(lockerId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val lockerName = document.getString("name")
                        val tempo = duration.toInt()

                        val lockerInfo = if (tempo > 16) {
                            "Armário: $lockerName\nPreço: $price    Duração: $tempo Min"
                        } else {
                            "Armário: $lockerName\nPreço: $price    Duração: $tempo H"
                        }
                        tvLockerInfo.text = lockerInfo
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

    private fun liberaArmario(lockerId: String, opc: Int) {
        val db = FirebaseFirestore.getInstance()

        if (opc == 1) {
            val intent = Intent(this, EncerrarReadNfcActivity::class.java)
            startActivity(intent)
            finish() // Opcional: para fechar a atividade atual
        } else if (opc == 2) {
            db.collection("unidade_de_locacao").document(lockerId)
                .update(mapOf("aberto" to false))
                .addOnSuccessListener {
                    Toast.makeText(this, "Armário fechado com sucesso.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao fechar o armário: $e", Toast.LENGTH_SHORT).show()
                }
        } else {
            db.collection("unidade_de_locacao").document(lockerId)
                .update(mapOf("aberto" to true))
                .addOnSuccessListener {
                    Toast.makeText(this, "Armário aberto com sucesso.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao abrir o armário: $e", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadImageFromPath(context: Context, imagePath: String, imageView: ImageView) {
        try {
            val pathPrefix = "/storage"
            val startIndex = imagePath.indexOf(pathPrefix)
            val trimmedPath = if (startIndex != -1) {
                imagePath.substring(startIndex)
            } else {
                imagePath
            }

            val file = File(trimmedPath)
            if (file.exists() && file.canRead()) {
                val bitmap = BitmapFactory.decodeFile(trimmedPath)
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                    imageView.visibility = View.VISIBLE
                } else {
                    Toast.makeText(
                        context,
                        "Falha ao decodificar o bitmap da imagem.",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("LoadImage", "Falha ao decodificar o bitmap da imagem: $trimmedPath")
                }
            } else {
                Toast.makeText(
                    context,
                    "O arquivo de imagem não existe ou não pode ser lido.",
                    Toast.LENGTH_LONG
                ).show()
                Log.e(
                    "LoadImage",
                    "O arquivo de imagem não existe ou não pode ser lido: $trimmedPath"
                )
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao carregar a imagem: ${e.message}", Toast.LENGTH_LONG)
                .show()
            Log.e("LoadImage", "Erro ao carregar a imagem: ${e.message}")
        }
    }
}

