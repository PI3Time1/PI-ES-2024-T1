package br.com.sentinellock

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private lateinit var eTextEmail: TextInputEditText
    private lateinit var eTextPassword: TextInputEditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonContWithoutRegistr: Button
    private lateinit var buttonRegister: Button

    // Método executado ao criar a atividade
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicialização dos componentes de interface do usuário e autenticação Firebase
        initializeViews()
        auth = FirebaseAuth.getInstance()

        // Configuração dos listeners de clique dos botões
        setupClickListeners()
    }

    // Método executado quando a atividade fica visível ao usuário
    override fun onStart() {
        super.onStart()
        // Verificar se o usuário já está logado
        checkCurrentUser()
    }

    // Inicializa os componentes de interface do usuário
    private fun initializeViews() {
        eTextEmail = findViewById(R.id.eTextEmail)
        eTextPassword = findViewById(R.id.eTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonContWithoutRegistr = findViewById(R.id.buttonContWithoutRegistr)
        buttonRegister = findViewById(R.id.buttonRegister)
    }

    // Configura os listeners de clique dos botões
    private fun setupClickListeners() {
        buttonLogin.setOnClickListener {
            val email = eTextEmail.text.toString()
            val password = eTextPassword.text.toString()

            if (isValidInputs()) {
                signIn(email, password)
            } else {
                showToast("Preencha todos os campos!")
            }
        }

        buttonContWithoutRegistr.setOnClickListener {
            navigateToTelaArmarioActivity()
        }

        buttonRegister.setOnClickListener {
            navigateToRegisterActivity()
        }
    }

    // Verifica se o usuário já está logado
    private fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            navigateToTelaArmarioActivity()
            finish()
        }
    }

    // Realiza o login com e-mail e senha
    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && !user.isEmailVerified) {
                        showToast("Por favor, verifique seu e-mail antes de fazer login.")
                        auth.signOut()
                    } else {
                        showToast("Login bem-sucedido!")
                        navigateToTelaArmarioActivity()
                        finish()
                    }
                } else {
                    showLoginError()
                }
            }
    }

    // Verifica se os campos de entrada são válidos
    private fun isValidInputs(): Boolean {
        val email = eTextEmail.text.toString()
        val password = eTextPassword.text.toString()
        return email.isNotEmpty() && password.isNotEmpty()
    }

    // Exibe um Toast com a mensagem fornecida
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Exibe uma mensagem de erro de login
    private fun showLoginError() {
        showToast("Autenticação falhou. Tente novamente!")
    }

    // Navega para a MainActivity
    private fun navigateToTelaArmarioActivity() {
        val intent = Intent(this, TelaArmarioActivity::class.java)
        startActivity(intent)
    }

    // Navega para a RegisterActivity
    private fun navigateToRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}