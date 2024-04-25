package br.com.sentinellock

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.android.material.textfield.TextInputLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// FUNCTIONS
import com.google.firebase.functions.ktx.functions

class AddCardActivity : AppCompatActivity() {
    // Activity Cadastrar Novo Cartao para usuario atual

    // Variavel de functions do firebase -- nao funciona por causa do import
    private lateinit var functions: FirebaseFunctions
    // Variaveis dos botoes voltar e adicionar novo cartao
    private lateinit var buttonBack: Button
    private lateinit var buttonAddCard: Button
    // Variaveis de adicao de cartao
    private lateinit var editTextNumeroCartao: TextInputEditText
    private lateinit var editTextDataValidade: TextInputEditText
    private lateinit var editTextCVV: TextInputEditText
    private lateinit var editTextNomeCartao: TextInputEditText
    // Variavel de usuario
    private lateinit var auth: FirebaseAuth

    // Inicio
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastrar_cartao)

        // Inicialização do Firebase Functions
        functions = com.google.firebase.ktx.Firebase.functions("southamerica-east1")

        // Inicialização das visualizacoes
        initializeViews()

        // Ouvintes de clique
        setupClickListeners()

        // Configura dados de entrada
        areInfoCorrect()

        // Verifica se usuario esta autenticado
        auth = com.google.firebase.Firebase.auth

    }

    // InitializeViews
    private fun initializeViews() {

        // Botao voltar
        buttonBack = findViewById(R.id.buttonBack)
        // Botao adicionar novo cartao
        buttonAddCard = findViewById(R.id.buttonAddCard)

        // Obtem as informacoes digitadas pelo usuario
        editTextNumeroCartao = findViewById(R.id.editTextNumeroCartao)
        editTextDataValidade = findViewById(R.id.editTextDataValidade)
        editTextCVV = findViewById(R.id.editTextCVV)
        editTextNomeCartao = findViewById(R.id.editTextNomeCartao)
    }

    // OUVINTES
    private fun setupClickListeners() {
        // Ouvinte para voltar para a atividade de gerenciar cartoes
        buttonBack.setOnClickListener {
            backToManageCards()
        }

        // Ouvinte para adicionar o cartao
        buttonAddCard.setOnClickListener {

            // VERIFICAR SE O USUARIO ESTA AUTENTICADO
            val currentUser = auth.currentUser
            if (currentUser != null) {

                // Verifica se os campos de entrada são válidos
                if (isInfoValid()) {
                    // Objeto card
                    val card = Card(
                        numeroCartao = editTextNumeroCartao.text.toString(),
                        nomeTitular = editTextNomeCartao.text.toString(),
                        dataExpiracao = editTextDataValidade.text.toString(),
                        cvv = editTextCVV.text.toString(),
                    )

                    // Chama a função de registro em uma coroutine
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val result = registerCard(card, currentUser.uid)
                            // Exibe uma mensagem de sucesso após o registro
                            showAlert(result)
                        } catch (e: Exception) {
                            // Trata qualquer exceção ocorrida durante o registro
                            Log.e(TAG, "Erro durante o registro: ${e.message}")
                            showAlert("${e.message}")
                        }
                    }
                } else {
                    // Mensagem de campos nao preenchidos
                    showAlert("Preencha todos os campos! Todos os campos são obrigatórios.")
                }
            } else {
                // Usuario nao esta autenticado!!
                showAlert("Usuário não autenticado! Por favor, faça login.")
            }
        }
    }


    // Checa se as informacoes digitadas pelo usuario estao corretas (dentro do padrao)
    private fun areInfoCorrect() {
        val cardNumber: TextInputLayout = findViewById(R.id.etNumeroCartao)
        val cardDate: TextInputLayout = findViewById(R.id.etDataValidade)
        val cardCVV: TextInputLayout = findViewById(R.id.etCVV)
        val cardName: TextInputLayout = findViewById(R.id.etNomeCartao)

        // Verifica o numero do cartao
        cardNumber.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val number = cardNumber.editText?.text?.toString()
                if (number != null && number.isNotEmpty() && !number.matches(Regex("^[0-9]*\$"))) {
                    cardNumber.error = "Digite somente números."
                } else {
                    cardNumber.error = null
                }
            }
        }

        // TIRAR DATA -- DIA-MES-ANO ---> MES-ANO - codigo pronto mas nao upado
        cardDate.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val date = cardDate.editText.toString()
                if (date.isNotEmpty()) {
                    cardDate.error = "Inválido"
                } else {
                    cardDate.error = null
                }
            }
        }

        // Verifica o CVV
        cardCVV.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val CVV = cardCVV.editText.toString()
                if (CVV.isNotEmpty() && CVV.length != 3) {
                    cardCVV.error = "O CVV deve ter 3 caracteres"
                } else {
                    cardCVV.error = null
                }
            }
        }

        // Verifica o nome do titular
        cardName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val name = cardName.editText.toString()
                if (name.isNotEmpty() && name.length < 5) {
                    cardName.error = "Nome de titular inválido."
                } else {
                    cardName.error = null
                }
            }
        }
    }

    // Registra cartão no Firebase
    private suspend fun registerCard(card: Card, uid: String): String {
        // Monta os dados do cartão para enviar para a função no Firebase
        val data = hashMapOf(
            "numeroCartao" to card.numeroCartao,
            "nomeTitular" to card.nomeTitular,
            "dataExpiracao" to card.dataExpiracao,
            "cvv" to card.cvv,
            "uid" to uid
        )

        // Chama a função de registro no Firebase Functions e aguarda a resposta
        val result = functions
            .getHttpsCallable("funcCadastrarCartao")
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
                dialog.dismiss()
            }
        val alertDialog = builder.create()
        alertDialog.show()

        // Personaliza a cor do botão positivo
        val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setTextColor(ContextCompat.getColor(this, R.color.green_500))
    }

    // Valida se todos os campos estao validos
    private fun isInfoValid(): Boolean {
        val cardnumber = editTextNumeroCartao.text.toString()
        val carddate = editTextDataValidade.text.toString()
        val cardcvv = editTextCVV.text.toString()
        val cardname = editTextNomeCartao.text.toString()

        // Verifica se algum campo esta vazio
        if (cardnumber.isEmpty() || carddate.isEmpty() || cardcvv.isEmpty() || cardname.isEmpty())
        {
            return false
        }

        return true
    }

    // Voltar para gerenciar cartoes
    private fun backToManageCards() {
        val intent = Intent(this, CardsActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Constante para TAG de adicao de cartao
    companion object {
        private const val TAG = "AddCardActivity"
    }

}

// Classe de dados do cartao
data class Card(
    val numeroCartao: String,
    val nomeTitular: String,
    val dataExpiracao: String,
    val cvv: String
)