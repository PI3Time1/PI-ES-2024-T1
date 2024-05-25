package br.com.sentinellock

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    // Declaração das variáveis necessárias
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var loadingScreen: View

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
        firestore = FirebaseFirestore.getInstance() // Inicialização do Firestore

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
        loadingScreen = findViewById(R.id.loadingScreen) // Inicialização do indicador de carregamento
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
                showLoadingScreen() // Mostra o indicador de carregamento
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
        // Mostra o indicador de carregamento
        showLoadingScreen()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            currentUser.let { user ->
                val userId = user.uid

                // Acessa o documento do usuário no Firestore
                firestore.collection("pessoas").document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        // Oculta o indicador de carregamento
                        hideLoadingScreen()

                        if (document.exists()) {
                            // Extrai dados do documento
                            val isAdm = document.getBoolean("isAdm") ?: false
                            if (isAdm) {
                                // O usuário é administrador
                                Log.d("checkCurrentUser", "Usuário é administrador.")
                                navigateToMenuGerente()
                                finish()
                            } else {
                                // O usuário não é administrador
                                Log.d("checkCurrentUser", "Usuário não é administrador.")
                                navigateToAlugarArmarioActivity()
                                finish()
                            }
                        } else {
                            // O documento não existe
                            showAlert("Usuário não encontrado.")
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Oculta o indicador de carregamento
                        hideLoadingScreen()
                        // Lida com a falha
                        showAlert("Erro ao acessar o Firestore: ${exception.message}")
                    }
            }
        } else {
            // Oculta o indicador de carregamento se não houver usuário logado
            hideLoadingScreen()
        }
    }

    // Realiza o login com e-mail e senha
    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                hideLoadingScreen() // Oculta o indicador de carregamento quando a tarefa é concluída
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Acessa o documento do usuário no Firestore para verificar se é administrador
                        val userId = user.uid
                        firestore.collection("pessoas").document(userId)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val isAdm = document.getBoolean("isAdm") ?: false
                                    if (isAdm) {
                                        // Se o login for bem-sucedido, e for um administrador então navega para a tela do gerente
                                        Log.d(TAG, "Usuário é administrador.")
                                        navigateToMenuGerente()
                                        finish()
                                    } else {
                                        // Se o usuário não for administrador, verifica se o e-mail está verificado
                                        if (user.isEmailVerified) {
                                            // Se o e-mail estiver verificado, navega para a AlugarArmarioActivity
                                            navigateToAlugarArmarioActivity()
                                            finish()
                                        } else {
                                            // Se o e-mail não estiver verificado, exibe uma mensagem
                                            showAlert("Por favor, verifique seu e-mail antes de fazer login.")
                                            auth.signOut()
                                        }
                                    }
                                } else {
                                    // Documento não encontrado
                                    showAlert("Credenciais inválidas! Verifique seu email e senha e tente novamente.")
                                }
                            }
                    }
                } else {
                    // Se o login falhar, exibe uma mensagem de erro
                    showAlert("Credenciais inválidas! Verifique seu email e senha e tente novamente.")
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

    private fun navigateToMenuGerente() {
        val intent = Intent(this, MenuGerente::class.java)
        startActivity(intent)
    }

    private fun showLoadingScreen() {
        loadingScreen.visibility = View.VISIBLE
    }

    private fun hideLoadingScreen() {
        loadingScreen.visibility = View.GONE
    }

    // Constante para TAG de LoginActivity
    companion object {
        private const val TAG = "LoginActivity"
    }
}