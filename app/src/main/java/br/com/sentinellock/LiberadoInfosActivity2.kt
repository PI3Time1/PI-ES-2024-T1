package br.com.sentinellock

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class LiberadoInfosActivity2 : AppCompatActivity() {

    private lateinit var userId: String
    private lateinit var lockerId: String
    private var price: Double = 0.0
    private var duration: Long = 0
    private lateinit var imageFilePaths: List<String>
    private lateinit var imageView1: ImageView
    private lateinit var imageView2: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liberado_infos2)

        // Recebendo os dados da intent

        userId = intent.getStringExtra("userId") ?: ""
        lockerId = intent.getStringExtra("lockerId") ?: ""
        price = intent.getDoubleExtra("price", 0.0)
        duration = intent.getLongExtra("duration", 0)
        imageFilePaths = intent.getStringArrayListExtra("IMAGE_PATHS") ?: emptyList()
        Log.d("LiberadoInfosActivity2", "Image paths: $imageFilePaths")

        // Inicializando as views
        val userInfoTextView: TextView = findViewById(R.id.userInfoTextView)
        val lockerInfoTextView: TextView = findViewById(R.id.lockerInfoTextView)
        imageView1 = findViewById(R.id.imageView1)
        imageView2 = findViewById(R.id.imageView2)

        // Carregando as imagens
        when (imageFilePaths.size) {
            1 -> {
                loadImageFromPath(imageFilePaths[0], imageView1)
            }
            2 -> {
                loadImageFromPath(imageFilePaths[0], imageView1)
                loadImageFromPath(imageFilePaths[1], imageView2)
            }
            else -> {
                Toast.makeText(this, "Nenhuma imagem disponível", Toast.LENGTH_SHORT).show()
            }
        }

        // Buscando informações do usuário no Firebase
        val db = FirebaseFirestore.getInstance()
        db.collection("pessoas").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val userName = document.getString("nome")
                    val userPhone = document.getString("telefone")
                    val userCpf = document.getString("cpf")
                    val userInfo = "Nome: $userName\nTelefone: $userPhone\nCPF: $userCpf "
                    userInfoTextView.text = userInfo
                } else {
                    Toast.makeText(this, "Usuário não encontrado no Firebase", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao buscar informações do usuário: $e", Toast.LENGTH_SHORT).show()
            }

        // Buscando informações do armário no Firebase
        db.collection("unidade_de_locacao").document(lockerId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val lockerName = document.getString("name")
                    val lockerInfo = "Armário: $lockerName \n Preço: $price\n Duração: $duration"
                    lockerInfoTextView.text = lockerInfo
                } else {
                    Toast.makeText(this, "Armário não encontrado no Firebase", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao buscar informações do armário: $e", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadImageFromPath(imagePath: String, imageView: ImageView) {
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
}
