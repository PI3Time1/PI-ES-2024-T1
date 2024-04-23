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

    // Inicio
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gerenciar_cartoes)

        // Botoes voltar e adicionar novo cartao
        buttonBack = findViewById(R.id.buttonBack)
        buttonWantAddCard = findViewById(R.id.buttonWantAddCard)

        // Botao Voltar
        buttonBack.setOnClickListener {
            val intent = Intent(this, MapsActivity2::class.java)
            // Volta para a tela do mapa (MapsActivity2)
            startActivity(intent)
            finish()
        }

        // Botao Adicionar
        buttonWantAddCard.setOnClickListener {
            val intent = Intent(this, AddCardActivity::class.java)
            // Abre a tela de adicionar novo cartao
            startActivity(intent)
            finish()
        }

    }
}