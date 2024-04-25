package br.com.sentinellock

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class NoLoginProfile : AppCompatActivity() {

    // Declaração de variáveis
    private lateinit var buttonLogin : Button
    private lateinit var buttonCadastrar2 : Button

    // Listener para o menu de navegação inferior
    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_map -> {
                    // Lidar com a ação de mapa
                    return@OnNavigationItemSelectedListener true
                }
                R.id.action_look -> {
                    // Lidar com a ação de busca
                    return@OnNavigationItemSelectedListener true
                }
                R.id.action_profile -> {
                    // Lidar com a ação de perfil
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_login_profile)

        // Inicialização das variáveis de interface
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonCadastrar2 = findViewById(R.id.buttonCadastrar2)

        // Configuração do menu de navegação inferior
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        // Configuração dos botões de login e cadastro
        buttonLogin.setOnClickListener {
            // Inicia LoginActivity ao clicar no botão de login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        buttonCadastrar2.setOnClickListener {
            // Inicia RegisterActivity ao clicar no botão de cadastro
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
