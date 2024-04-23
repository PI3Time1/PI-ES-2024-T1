package br.com.sentinellock

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.android.material.textfield.TextInputLayout
// Imports ainda nao utilizados
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.Firebase

class CardsActivity : AppCompatActivity() {
    // Activity Cadastrar Novo Cartao para usuario atual
    // Variaveis dos botoes voltar e adicionar novo cartao
    private lateinit var buttonBack: Button
    private lateinit var buttonWantAddCard: Button

    // Variaveis de adicao de cartao
    private lateinit var nomeTitular: TextInputLayout
    private lateinit var finalCartao: TextInputLayout

    // Variavel de autenticacao
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String

    // Inicio
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gerenciar_cartoes)

        // Inicialização das visualizacoes
        initializeViews()

        // Ouvintes de clique
        setupClickListeners()

        // Verifica se usuario esta autenticado
        auth = com.google.firebase.Firebase.auth
        val uid = auth.currentUser?.uid
    }

    private fun initializeViews() {

        // Botao voltar
        buttonBack = findViewById(R.id.buttonBack)
        // Botao adicionar novo cartao
        buttonWantAddCard = findViewById(R.id.buttonWantAddCard)

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

    // Voltar para perfil --> ProfileActivity ainda não está aqui
    private fun backToProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Ir pra adicionar novo cartao
    private fun goToAddCard() {
        val intent = Intent(this, AddCardActivity::class.java)
        startActivity(intent)
        finish()
    }

}

