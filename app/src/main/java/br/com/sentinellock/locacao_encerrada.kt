package br.com.sentinellock

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class locacao_encerrada : AppCompatActivity() {
    // declaração das variaveis necessárias
    private lateinit var buttonBack : Button
    private lateinit var buttonBackToMenu : Button
    override fun onCreate(savedInstanceState: Bundle?) {
// Associa as variáveis às visualizações no layout XML
        buttonBack = findViewById(R.id.buttonBack)
        buttonBackToMenu = findViewById(R.id.buttonBackToMenu)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locacao_encerrada)
// Ao clicar no botão ele realiza a ação de ir para a tela do Menu do Gerente
        buttonBack.setOnClickListener {
            val intent = Intent(this,MenuGerente::class.java)
            startActivity(intent)
            finish()
        }
// Ao clicar no botão ele realiza a ação de ir para a tela do Menu do Gerente
        buttonBackToMenu.setOnClickListener {
            val intent = Intent(this,MenuGerente::class.java)
            startActivity(intent)
            finish()
        }

    }
}