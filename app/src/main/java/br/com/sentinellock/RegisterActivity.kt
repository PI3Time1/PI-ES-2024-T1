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

class RegisterActivity : AppCompatActivity() {

    // Declaração das variáveis necessárias
    private lateinit var functions: FirebaseFunctions

    private lateinit var buttonBackToSignIn: ImageButton
    private lateinit var buttonRegister: Button
    private lateinit var buttonSignIn: Button

    private lateinit var eTextName: TextInputEditText
    private lateinit var eTextEmail: TextInputEditText
    private lateinit var eTextPassword: TextInputEditText
    private lateinit var eTextAge: TextInputEditText
    private lateinit var eTextCPF: TextInputEditText
    private lateinit var eTextPhone: TextInputEditText

    // Método chamado quando a atividade é criada
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

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
        buttonRegister = findViewById(R.id.buttonRegister)
        buttonSignIn = findViewById(R.id.buttonSignIn)

        eTextName = findViewById(R.id.eTextName)
        eTextEmail = findViewById(R.id.eTextEmail)
        eTextPassword = findViewById(R.id.eTextPassword)
        eTextAge = findViewById(R.id.eTextAge)
        eTextCPF = findViewById(R.id.eTextCPF)
        eTextPhone = findViewById(R.id.eTextPhone)
    }

    // Configura os ouvintes de clique
    private fun setupClickListeners() {
        // Ouvinte para voltar para a atividade de login
        buttonBackToSignIn.setOnClickListener {
            navigateToLoginActivity()
        }

        // Ouvinte para ir para a atividade de login
        buttonSignIn.setOnClickListener {
            navigateToLoginActivity()
        }

        // Ouvinte para registrar o cliente
        buttonRegister.setOnClickListener {
            if (isValidInputs()) {
                // Cria um objeto cliente com os dados inseridos
                val client = Client(
                    nome = eTextName.text.toString(),
                    email = eTextEmail.text.toString(),
                    senha = eTextPassword.text.toString(),
                    cpf = eTextCPF.text.toString(),
                    telefone = eTextPhone.text.toString(),
                    dataNascimento = eTextAge.text.toString()
                )

                // Chama a função de registro em uma coroutine
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val result = registerClient(client)
                        // Exibe uma mensagem de sucesso após o registro
                        showAlert("Registro bem-sucedido: $result")
                    } catch (e: Exception) {
                        // Trata qualquer exceção ocorrida durante o registro
                        Log.e(TAG, "Erro durante o registro: ${e.message}")
                        showAlert("${e.message}")
                    }
                }
            } else {
                // Exibe uma mensagem de erro se algum campo estiver vazio
                showAlert("Preencha todos os campos!! Todos os campos são obrigatórios.")
            }
        }
    }

    // Configura as validações de entrada
    private fun setupInputValidations() {
        // Configura os ouvintes de foco para validar os campos de entrada
        val emailLayout: TextInputLayout = findViewById(R.id.etEmail)
        val passwordLayout: TextInputLayout = findViewById(R.id.etPassword)
        val CPFLayout: TextInputLayout = findViewById(R.id.etCPF)
        val phoneLayout: TextInputLayout = findViewById(R.id.etPhone)

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

        eTextPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val password = eTextPassword.text.toString()
                if (password.isNotEmpty() && password.length < 6) {
                    passwordLayout.error = "A senha deve ter pelo menos 6 caracteres"
                } else {
                    passwordLayout.error = null
                }
            }
        }

        eTextCPF.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val CPF = eTextCPF.text.toString()
                if (CPF.isNotEmpty() && CPF.length != 11) {
                    CPFLayout.error = "O CPF deve ter 11 caracteres"
                } else {
                    CPFLayout.error = null
                }
            }
        }

        eTextPhone.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val phone = eTextPhone.text.toString()
                if (phone.isNotEmpty() && phone.length != 11) {
                    phoneLayout.error = "O telefone deve ter 11 caracteres"
                } else {
                    phoneLayout.error = null
                }
            }
        }
    }

    // Registra o cliente no Firebase
    private suspend fun registerClient(client: Client): String {
        // Monta os dados do cliente para enviar para a função no Firebase
        val data = hashMapOf(
            "nome" to client.nome,
            "cpf" to client.cpf,
            "dataNascimento" to client.dataNascimento,
            "telefone" to client.telefone,
            "email" to client.email,
            "senha" to client.senha,
        )

        // Chama a função de registro no Firebase Functions e aguarda a resposta
        val result = functions
            .getHttpsCallable("funcCadastrarCliente")
            .call(data)
            .await()

        // Obtém os dados da resposta
        val responseData = result.data

        return if (responseData is String) {
            // Retorna a resposta se for uma string
            responseData
        } else {
            // Log de erro se a resposta não for uma string
            Log.e(TAG, "Resposta inesperada do backend: $responseData")
            "Erro ao processar resposta do servidor"
        }
    }

    // Método para exibir o AlertDialog
    private fun showAlert(message: String) {
        // Cria um AlertDialog com a mensagem fornecida
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialogStyle)
        builder.setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                // Se o registro for bem-sucedido, navega de volta para a atividade de login
                if(message == "Registro bem-sucedido: Cadastro realizado com sucesso!"){
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
        // Verifica se algum campo está vazio
        val name = eTextName.text.toString()
        val email = eTextEmail.text.toString()
        val password = eTextPassword.text.toString()
        val age = eTextAge.text.toString()
        val cpf = eTextCPF.text.toString()
        val phone = eTextPhone.text.toString()

        return !(name.isEmpty() || email.isEmpty() || password.isEmpty() ||
                age.isEmpty() || cpf.isEmpty() || phone.isEmpty())
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

    // Constante para TAG de registro
    companion object {
        const val TAG = "RegisterActivity"
    }
}

// Classe de dados que representa um cliente
data class Client(
    val nome: String,
    val email: String,
    val senha: String,
    val cpf: String,
    val telefone: String,
    val dataNascimento: String
)
