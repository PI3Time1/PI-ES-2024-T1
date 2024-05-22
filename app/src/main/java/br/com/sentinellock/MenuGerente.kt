package br.com.sentinellock

import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MenuGerente : AppCompatActivity() {

    private lateinit var LiberarLocacao: Button
    private lateinit var EncerrarLocacao: Button
    private lateinit var buttonLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_gerente)

        LiberarLocacao = findViewById(R.id.LiberarLocacao)
        EncerrarLocacao = findViewById(R.id.EncerrarLocacao)
        buttonLogout = findViewById(R.id.buttonLogout)

        LiberarLocacao.setOnClickListener {
            val intent = Intent(this, LerQrcodeActivity::class.java)
            startActivity(intent)
            finish()
        }
        EncerrarLocacao.setOnClickListener {
            val intent = Intent(this, ReadNFCActivity::class.java)
            startActivity(intent)
            finish()
        }
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


