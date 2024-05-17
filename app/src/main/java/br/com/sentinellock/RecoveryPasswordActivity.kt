package br.com.sentinellock

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RecoveryPasswordActivity : AppCompatActivity() {

    // Declaração das variáveis necessárias
    private lateinit var functions: FirebaseFunctions

    private lateinit var buttonBackToSignIn: ImageButton
    private lateinit var buttonSendRecoveryPassword: Button

    private lateinit var eTextEmail: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recovery_password)

        // Inicialização do Firebase Functions com a região South America
        functions = Firebase.functions("southamerica-east1")

        // Inicialização das visualizações
        initializeViews()

        // Configuração dos ouvintes de clique
        setupClickListeners()

        // Configuração das validações de entrada
        setupInputValidations()
    }

    // Inicializa as visualizações da interface do usuário
    private fun initializeViews() {
        // Associa as variáveis às visualizações no layout XML
        buttonBackToSignIn = findViewById(R.id.buttonBackToSignIn)
        buttonSendRecoveryPassword = findViewById(R.id.buttonSendRecoveryPassword)

        eTextEmail = findViewById(R.id.eTextEmail)
    }

    // Configura os ouvintes de clique
    private fun setupClickListeners() {
        // Ouvinte para voltar para a atividade de login
        buttonBackToSignIn.setOnClickListener {
            navigateToLoginActivity()
        }

        // Ouvinte para recuperar senha
        buttonSendRecoveryPassword.setOnClickListener {
            if (isValidInputs()) {
                // Obtém o e-mail fornecido pelo usuário
                val email = eTextEmail.text.toString()

                // Chama a função de recuperação em uma coroutine
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val result = recoveryPassword(email)
                        // Exibe uma mensagem com o resultado da recuperação
                        showAlert(result)
                    } catch (e: Exception) {
                        // Trata qualquer exceção ocorrida durante a recuperação
                        Log.e(RegisterActivity.TAG, "Erro durante a recuperação de senha: ${e.message}")
                        showAlert("${e.message}")
                    }
                }
            } else {
                // Exibe uma mensagem de erro se o campo de e-mail estiver vazio
                showAlert("Preencha todos os campos!! Todos os campos são obrigatórios.")
            }
        }
    }

    // Configura as validações de entrada
    private fun setupInputValidations() {
        // Configura um ouvinte de foco para validar o campo de e-mail
        val emailLayout: TextInputLayout = findViewById(R.id.etEmail)

        eTextEmail.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val email = eTextEmail.text.toString()
                if (email.isNotEmpty() && !isValidEmail(email)) {
                    emailLayout.error = "Formato de e-mail inválido"
                } else {
                    emailLayout.error = null
                }
            }
        }
    }

    // Função para recuperar a senha
    private suspend fun recoveryPassword(email: String): String {
        // Monta os dados necessários para enviar para a função no Firebase
        val data = hashMapOf(
            "email" to email,
        )

        // Chama a função de recuperação no Firebase Functions e aguarda a resposta
        val result = functions
            .getHttpsCallable("funcRecuperarSenha")
            .call(data)
            .await()

        // Obtém os dados da resposta
        val responseData = result.data

        return if (responseData is String) {
            // Retorna a resposta se for uma string
            responseData
        } else {
            // Log de erro se a resposta não for uma string
            Log.e(RecoveryPasswordActivity.TAG, "Resposta inesperada do backend: $responseData")
            "Erro ao processar resposta do servidor"
        }
    }

    // Método para exibir o AlertDialog
    private fun showAlert(message: String) {
        // Cria um AlertDialog com a mensagem fornecida
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialogStyle)
        builder.setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                // Se o e-mail de recuperação for enviado com sucesso, navega de volta para a atividade de login
                if(message.startsWith("E-mail de recuperação de senha enviado para:")){
                    navigateToLoginActivity()
                }
                dialog.dismiss()
            }
        val alertDialog = builder.create()
        alertDialog.show()

        // Personaliza a cor do botão positivo
        val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setTextColor(ContextCompat.getColor(this, R.color.green_500))
    }

    // Verifica se os campos de entrada são válidos
    private fun isValidInputs(): Boolean {
        // Verifica se o campo de e-mail não está vazio
        val email = eTextEmail.text.toString()
        return !(email.isEmpty())
    }

    // Navega para a atividade de login
    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Verifica se o e-mail é válido
    private fun isValidEmail(email: String): Boolean {
        // Verifica se o formato do e-mail é válido usando uma expressão regular
        val emailRegex = Regex("^\\S+@\\S+\\.\\S+\$")
        return emailRegex.matches(email)
    }

    // Constante para TAG de recuperação de senha
    companion object {
        private const val TAG = "RecoveryPasswordActivity"
    }
}