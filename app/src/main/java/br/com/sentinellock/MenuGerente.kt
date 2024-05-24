package br.com.sentinellock

import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MenuGerente : AppCompatActivity() {
// inicialização das variavéis
    private lateinit var LiberarLocacao: Button
    private lateinit var EncerrarLocacao: Button
    private lateinit var buttonLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_gerente)
// Associa as variáveis às visualizações no layout XML
        LiberarLocacao = findViewById(R.id.LiberarLocacao)
        EncerrarLocacao = findViewById(R.id.EncerrarLocacao)
        buttonLogout = findViewById(R.id.buttonLogout)
// botão que redireciona o usuário para a tela LERQRCODE
        LiberarLocacao.setOnClickListener {
            val intent = Intent(this, LerQrcodeActivity::class.java)
            startActivity(intent)
            finish()
        }
        // botão que redireciona o usuário para a tela READNFC
        EncerrarLocacao.setOnClickListener {
            val intent = Intent(this, ReadNFCActivity::class.java)
            startActivity(intent)
            finish()
        }
        // botão que faz o logout do usuário
        buttonLogout.setOnClickListener {
            // Faz o logout do Firebase Auth
            FirebaseAuth.getInstance().signOut()

            // Redireciona o usuário de volta à tela de login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}


