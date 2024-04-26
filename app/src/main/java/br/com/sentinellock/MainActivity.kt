// Pacote onde a classe MainActivity está localizada
package br.com.sentinellock

// Importações necessárias para as funcionalidades do Android e Firebase
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

// Declaração da classe MainActivity, que é uma atividade principal do aplicativo
class MainActivity : AppCompatActivity() {

    // Declaração de uma instância de FirebaseAuth, que será usada para autenticação do Firebase
    private lateinit var auth: FirebaseAuth

    // Declaração de uma instância de Button, para o botão de logout
    private lateinit var buttonLogOut: Button

    // Método chamado quando a atividade é criada
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Define o layout da atividade a partir do arquivo XML activity_main.xml
        setContentView(R.layout.activity_main)

        // Inicializa o botão de logout procurando-o na hierarquia de views do layout
        buttonLogOut = findViewById(R.id.buttonLogOut)

        // Inicializa a instância de FirebaseAuth
        auth = Firebase.auth

        // Configura um listener de clique para o botão de logout
        buttonLogOut.setOnClickListener {
            // Efetua o logout do usuário
            auth.signOut()
            // Cria uma Intent para iniciar a LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            // Inicia a LoginActivity
            startActivity(intent)
            // Finaliza a atividade atual (MainActivity)
            finish()
        }
    }
}
