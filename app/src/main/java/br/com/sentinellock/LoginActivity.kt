package br.com.sentinellock

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    // Declaração das variáveis necessárias
    private lateinit var auth: FirebaseAuth

    private lateinit var buttonLogin: Button
    private lateinit var buttonContWithoutRegistr: Button
    private lateinit var buttonRegister: Button
    private lateinit var buttonRecoveryPassword: Button

    private lateinit var eTextEmail: TextInputEditText
    private lateinit var eTextPassword: TextInputEditText

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
        // Associa as variáveis às visualizações no layout XML
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonContWithoutRegistr = findViewById(R.id.buttonContWithoutRegistr)
        buttonRegister = findViewById(R.id.buttonRegister)
        buttonRecoveryPassword = findViewById(R.id.buttonRecoveryPassword)

        eTextEmail = findViewById(R.id.eTextEmail)
        eTextPassword = findViewById(R.id.eTextPassword)
    }

    // Configura os listeners de clique dos botões
    private fun setupClickListeners() {
        buttonLogin.setOnClickListener {
            // Obtém o e-mail e a senha fornecidos pelo usuário e realiza o login
            val email = eTextEmail.text.toString()
            val password = eTextPassword.text.toString()

            if (isValidInputs()) {
                signIn(email, password)
            } else {
                showAlert("Preencha todos os campos!")
            }
        }

        buttonContWithoutRegistr.setOnClickListener {
            // Navega para a MapsActivity
            navigateToAlugarArmarioActivity()
        }

        buttonRegister.setOnClickListener {
            // Navega para a RegisterActivity
            navigateToRegisterActivity()
        }

        buttonRecoveryPassword.setOnClickListener {
            // Navega para a RecoveryPasswordActivity
            navigateToRecoveryPasswordActivity()
        }
    }

    // Verifica se o usuário já está logado
    private fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Se o usuário já estiver logado, navega diretamente para a MapsActivity
            navigateToAlugarArmarioActivity()
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
                        // Se o e-mail não estiver verificado, exibe uma mensagem
                        showAlert("Por favor, verifique seu e-mail antes de fazer login.")
                        auth.signOut()
                    } else {
                        // Se o login for bem-sucedido, navega para a MapsActivity
                        navigateToAlugarArmarioActivity()
                        finish()
                    }
                } else {
                    // Se o login falhar, exibe uma mensagem de erro
                    showLoginError()
                }
            }
    }

    // Verifica se os campos de entrada são válidos
    private fun isValidInputs(): Boolean {
        // Verifica se o campo de e-mail e senha não estão vazios
        val email = eTextEmail.text.toString()
        val password = eTextPassword.text.toString()
        return email.isNotEmpty() && password.isNotEmpty()
    }

    // Método para exibir o AlertDialog
    private fun showAlert(message: String) {
        // Cria um AlertDialog com a mensagem fornecida
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialogStyle)
        builder.setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                // Fecha o AlertDialog ao clicar no botão OK
                dialog.dismiss()
            }
        val alertDialog = builder.create()
        alertDialog.show()

        // Personaliza a cor do botão positivo
        val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setTextColor(ContextCompat.getColor(this, R.color.green_500))
    }

    // Exibe uma mensagem de erro de login
    private fun showLoginError() {
        showAlert("Autenticação falhou. Tente novamente!")
    }

    // Navega para a MapsActivity
    private fun navigateToAlugarArmarioActivity() {
        val intent = Intent(this, TelaArmarioActivity::class.java)
        startActivity(intent)
    }

    // Navega para a RecoveryPasswordActivity
    private fun navigateToRecoveryPasswordActivity() {
        val intent = Intent(this, RecoveryPasswordActivity::class.java)
        startActivity(intent)
    }

    // Navega para a RegisterActivity
    private fun navigateToRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    // Constante para TAG de LoginActivity
    companion object {
        private const val TAG = "LoginActivity"
    }
}