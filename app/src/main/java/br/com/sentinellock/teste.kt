package br.com.sentinellock

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

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

        // Inicialização do Firebase Functions
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
            // Verifica se os campos de entrada são válidos
            if (isValidInputs()) {
                // Cria um objeto Cliente com os dados inseridos
                val client = Client(
                    nome = eTextName.text.toString(),
                    email = eTextEmail.text.toString(),
                    senha = eTextPassword.text.toString(),
                    cpf = eTextCPF.text.toString(),
                    telefone = eTextPhone.text.toString(),
                    dataNascimento = eTextAge.text.toString()
                )

                // Registra o cliente no Firebase
                registerClient(client)
                    .addOnCompleteListener { task ->
                        val e = task.exception
                        if (e != null) {
                            if (e.message == "UNKNOWN") {
                                // Exibe mensagem de sucesso
                                showToast("Cadastrado com sucesso! Um e-mail de verificação foi enviado para o e-mail: ${client.email}.")
                            } else {
                                // Exibe mensagem de erro técnico
                                showToast("Erro técnico! Entre em contato com o suporte.")
                            }
                        }
                    }
            } else {
                // Exibe mensagem se os campos não estiverem preenchidos
                showToast("Preencha todos os campos!! Todos os campos são obrigatórios.")
            }
        }
    }

    // Configura as validações de entrada
    private fun setupInputValidations() {
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
    private fun registerClient(client: Client): Task<String> {
        val data = hashMapOf(
            "nome" to client.nome,
            "cpf" to client.cpf,
            "dataNascimento" to client.dataNascimento,
            "telefone" to client.telefone,
            "email" to client.email,
            "senha" to client.senha,
        )

        return functions
            .getHttpsCallable("funcCadastrarCliente")
            .call(data)
            .continueWith { task ->
                val result = task.result?.data as String
                Log.d(TAG, result)
                result
            }
    }

    // Exibe uma mensagem toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    // Verifica se os campos de entrada são válidos
    private fun isValidInputs(): Boolean {
        val name = eTextName.text.toString()
        val email = eTextEmail.text.toString()
        val password = eTextPassword.text.toString()
        val age = eTextAge.text.toString()
        val cpf = eTextCPF.text.toString()
        val phone = eTextPhone.text.toString()

        // Verifica se algum campo está vazio
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() ||
            age.isEmpty() || cpf.isEmpty() || phone.isEmpty()
        ) {
            return false
        }

        return true
    }

    // Navega para a atividade de login
    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Verifica se o e-mail é válido
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^\\S+@\\S+\\.\\S+\$")
        return emailRegex.matches(email)
    }

    // Constante para TAG de registro
    companion object {
        private const val TAG = "RegisterActivity"
    }
}

// Classe de dados que representa um cliente
data class Client (
    val nome: String,
    val email: String,
    val senha: String,
    val cpf: String,
    val telefone: String,
    val dataNascimento: String
)