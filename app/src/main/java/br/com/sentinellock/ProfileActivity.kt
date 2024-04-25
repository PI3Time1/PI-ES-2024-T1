package br.com.sentinellock

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private var selectedItemId: Int = R.id.action_profile

    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_map -> {
                    // Handle map action
                    return@OnNavigationItemSelectedListener true
                }
                R.id.action_look -> {
                    // Handle search action
                    return@OnNavigationItemSelectedListener true
                }
                R.id.action_profile -> {
                    // Handle profile action
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    // Declaração de variáveis
    private lateinit var buttonManageCard: Button
    private lateinit var buttonLogout2: Button
    private lateinit var textViewNome: TextView
    private lateinit var textViewEmail: TextView
    private lateinit var textViewCpf: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Verifica se o usuário está logado
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            // Se o usuário não estiver logado, abre a NoLoginProfileActivity e fecha esta
            startActivity(Intent(this, NoLoginProfile::class.java))
            finish()
            return
        }

        savedInstanceState?.getInt("selectedItemId")?.let {
            selectedItemId = it
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        bottomNavigationView.selectedItemId = selectedItemId

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_look -> {
                    startActivity(Intent(this, TelaArmarioActivity::class.java))
                    true
                }
                R.id.action_map -> {
                    startActivity(Intent(this, MapsActivity2::class.java))
                    true
                }
                R.id.action_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Inicialização das variáveis de interface e Firebase
        buttonManageCard = findViewById(R.id.buttonManageCard)
        buttonLogout2 = findViewById(R.id.buttonLogout2)
        textViewNome = findViewById(R.id.TextViewNome)
        textViewEmail = findViewById(R.id.TextViewEmail)
        textViewCpf = findViewById(R.id.TextViewCpf)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Obtém o usuário atualmente autenticado
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userId = user.uid

            // Acessa o documento do usuário no Firestore
            firestore.collection("pessoas").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        // Extrai dados do documento
                        val nome = document.getString("nome")
                        val email = document.getString("email")
                        val cpf = document.getString("cpf")

                        // Exibe os dados nos TextViews
                        textViewNome.text = nome
                        textViewEmail.text = email
                        textViewCpf.text = cpf
                    } else {
                        // O documento não existe
                    }
                }
                .addOnFailureListener { exception ->
                    // Lida com falhas ao acessar o Firestore
                }
        }

        // Configuração do botão de logout
        auth = Firebase.auth
        buttonLogout2.setOnClickListener {
            // Realiza logout do usuário
            auth.signOut()
            // Redireciona para a tela de login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

//         Configuração do botão para gerenciar cartões (comentado)
        buttonManageCard.setOnClickListener{
            val intent = Intent(this,CardsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}