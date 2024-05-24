package br.com.sentinellock

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AbrirOuEncerrar : AppCompatActivity() {
// declaração das variaveis necessárias
    private lateinit var buttonBack : Button
    private lateinit var AbrirMomentaneamente : Button
    private lateinit var EncerrarLocacao : Button

    // Método executado ao criar a atividade
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_abrir_ou_encerrar)
// Associa as variáveis às visualizações no layout XML
        buttonBack = findViewById(R.id.buttonBack)
        AbrirMomentaneamente = findViewById(R.id.AbrirMomentaneamente)
        EncerrarLocacao = findViewById(R.id.EncerrarLocacao)



// Ao clicar no botão ele realiza a ação de voltar pra tela do gerente
        buttonBack.setOnClickListener {
            val intent = Intent(this,MenuGerente::class.java)
            startActivity(intent)
            finish()
        }
// Ao clicar no botão ele realiza a ação de ir para a tela do ReadNFC
        AbrirMomentaneamente.setOnClickListener {
            val intent = Intent(this,ReadNFCActivity ::class.java)
            startActivity(intent)
            finish()
        }
// Ao clicar no botão ele realiza a ação de ir para a tela de EncerrarLocação

        EncerrarLocacao.setOnClickListener {
            val intent = Intent(this, EncerrarReadNfcActivity::class.java)
            startActivity(intent)
                finish()
        }
    }
}