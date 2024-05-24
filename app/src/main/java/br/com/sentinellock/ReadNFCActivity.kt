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

class ReadNFCActivity : AppCompatActivity() {

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var tvUserInfo: TextView
    private lateinit var tvLockerInfo: TextView
    private lateinit var imageView1: ImageView
    private lateinit var imageView2: ImageView
    private lateinit var loadingDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_nfcactivity)

        tvUserInfo = findViewById(R.id.userInfoTextView)
        tvLockerInfo = findViewById(R.id.lockerInfoTextView)
        imageView1 = findViewById(R.id.imageView1)
        imageView2 = findViewById(R.id.imageView2)

        loadingDialog = Dialog(this)
        loadingDialog.setContentView(R.layout.dialog_loading)
        loadingDialog.setCancelable(false)
        loadingDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

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

                loadingDialog.dismiss()
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
                    Log.d("NFC_TAG", "User ID: ${records[0].payload.decodeToString()},${records[1].payload.decodeToString()},${records[2].payload.decodeToString()},${records[3].payload.decodeToString()},${records[4].payload.decodeToString()},${records[5].payload.decodeToString()}")
                    val record1 = records[2]
                    val record2 = if (records.size > 1) records[3] else null
                    var userId = record1.payload.decodeToString()
                    var lockerId = record2?.payload?.decodeToString()
                    var duration = records[5].payload.decodeToString()
                    var price = records[4].payload.decodeToString()
                    val imagePath1 = records[0].payload.decodeToString().trim()
                    val imagePath2 = records[1].payload.decodeToString().trim()

                    // Logging das informações lidas da tag

                    price = price.trim { it <= ' ' }.substringAfter("price: ")
                    duration = duration.trim { it <= ' ' }.substringAfter("duration: ")
                    userId = userId.trim { it <= ' ' }.substringAfter("userId: ")
                    lockerId = lockerId?.trim { it <= ' ' }?.substringAfter("lockerId: ")

                    Log.d("NFC_TAG", "User ID: $userId")
                    Log.d("NFC_TAG", "Locker ID: $lockerId")

                    // Exibindo os valores na tela
                    displayUserInfo(userId)
                    displayLockerInfo(lockerId, price, duration)

                    if(imagePath1 != null || imagePath2 != null){
                        if(imagePath2 != null){
                            loadImageFromPath(this,imagePath1, imageView1)
                            loadImageFromPath(this,imagePath2, imageView2)
                        }
                        else{
                            loadImageFromPath(this,imagePath1, imageView1)
                        }
                    }else{
                        Toast.makeText(this, "Nenhuma imagem foi lida", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            ndef.close()
        }
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
                    Toast.makeText(this, "Usuário não encontrado no Firebase", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao buscar informações do usuário: $e", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayLockerInfo(lockerId: String?,price:Any, duration:Any ) {
        if (lockerId != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("unidade_de_locacao").document(lockerId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val lockerName = document.getString("name")

                        val lockerInfo = "Armário: $lockerName\nPreço: $price    Duração: $duration"
                        tvLockerInfo.text = lockerInfo
                    } else {
                        Toast.makeText(this, "Armário não encontrado no Firebase", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao buscar informações do armário: $e", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "ID do armário não encontrado", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun loadImageFromPath(context: Context, imagePath: String, imageView: ImageView) {
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
        Log.d("LoadImage", "Path: $trimmedPath, File exists: ${file.exists()}, Can read: ${file.canRead()}")

        if (file.exists() && file.canRead()) {
            val bitmap = BitmapFactory.decodeFile(trimmedPath)
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
                imageView.visibility = View.VISIBLE // tornar o ImageView visível
                Log.d("LoadImage", "Imagem carregada com sucesso: $bitmap")
            } else {
                Toast.makeText(context, "Falha ao decodificar o bitmap da imagem.", Toast.LENGTH_LONG).show()
                Log.e("LoadImage", "Falha ao decodificar o bitmap da imagem: $trimmedPath")
            }
        } else {
            Toast.makeText(context, "O arquivo de imagem não existe ou não pode ser lido.", Toast.LENGTH_LONG).show()
            Log.e("LoadImage", "O arquivo de imagem não existe ou não pode ser lido: $trimmedPath")
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Erro ao carregar a imagem: ${e.message}", Toast.LENGTH_LONG).show()
        Log.e("LoadImage", "Erro ao carregar a imagem: ${e.message}")
    }
}

