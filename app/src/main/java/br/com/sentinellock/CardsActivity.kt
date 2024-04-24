package br.com.sentinellock

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class CardsActivity : AppCompatActivity() {
    // Activity Cadastrar Novo Cartao para usuario atual

    // Variaveis dos botoes voltar e adicionar novo cartao
    private lateinit var buttonBack: Button
    private lateinit var buttonWantAddCard: Button

    // Variaveis de adicao de cartao
    private lateinit var nomeTitularView: TextInputLayout
    private lateinit var finalCartaoView: TextInputLayout

    // Variavel de autenticacao
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // Variaveis de dados
    private lateinit var tvNomeTitular: TextView



    // Inicio
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gerenciar_cartoes)

        // Inicialização das visualizacoes
        initializeViews()

        // Ouvintes de clique
        setupClickListeners()

        // Verifica se usuario esta autenticado
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userId = user.uid

            // Acessar o documento do usuário no Firestore ---- puxa direto de pessoas
                firestore.collection("pessoas").document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            // Extrair dados do documento
                            val nomeDoTitular = document.getString("nomeTitular")
                            val numeroDoCartao = document.getString("numeroCartao")

                            // Exibir os dados nos TextViews
                            nomeTitularView.editText?.setText(nomeDoTitular)
                            finalCartaoView.editText?.setText(numeroDoCartao)
                        } else {
                            // O documento não existe
                            showToast("Nenhum cartão encontrado.")
                        }
                    }
                    .addOnFailureListener { exception ->
                        showToast("Erro ao buscar cartões.")
                    }
            }

    }

    private fun initializeViews() {

        // Botao voltar
        buttonBack = findViewById(R.id.buttonBack)
        // Botao adicionar novo cartao
        buttonWantAddCard = findViewById(R.id.buttonWantAddCard)

        // TextView do nome do titular
        tvNomeTitular = findViewById(R.id.tvNomeTitular)
    }

    private fun setupClickListeners() {
        // Ouvinte para voltar para meu perfil
        buttonBack.setOnClickListener {
            backToProfile()
        }

        // Ouvinte para ir pra tela de adicionar novo cartao
        buttonWantAddCard.setOnClickListener {
            goToAddCard()
        }

    }

    // Voltar para perfil --> Substituir MapsActivity2 por ProfileActivity!
    private fun backToProfile() {
        val intent = Intent(this, MapsActivity2::class.java)
        startActivity(intent)
        finish()
    }

    // Ir pra adicionar novo cartao
    private fun goToAddCard() {
        val intent = Intent(this, AddCardActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Mensagem toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}

