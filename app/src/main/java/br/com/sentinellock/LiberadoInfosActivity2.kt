package br.com.sentinellock

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class LiberadoInfosActivity2 : AppCompatActivity() {

    private lateinit var userId: String
    private lateinit var lockerId: String
    private var price: Double = 0.0
    private var duration: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liberado_infos2)

        // Recebendo os dados da intent
        userId = intent.getStringExtra("userId") ?: ""
        lockerId = intent.getStringExtra("lockerId") ?: ""
        price = intent.getDoubleExtra("price", 0.0)
        duration = intent.getLongExtra("duration", 0)

        // Inicializando as views
        val userInfoTextView: TextView = findViewById(R.id.userInfoTextView)
        val lockerInfoTextView: TextView = findViewById(R.id.lockerInfoTextView)

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
                    val lockerInfo = "Armário: $lockerName"
                    lockerInfoTextView.text = lockerInfo
                } else {
                    Toast.makeText(this, "Armário não encontrado no Firebase", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao buscar informações do armário: $e", Toast.LENGTH_SHORT).show()
            }
    }
}
