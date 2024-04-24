package br.com.sentinellock

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.android.material.textfield.TextInputLayout
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.functions.FirebaseFunctions

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
        editTextNumeroCartao = findViewById(R.id.etNumeroCartao)
        editTextDataValidade = findViewById(R.id.etDataValidade)
        editTextCVV = findViewById(R.id.etCVV)
        editTextNomeCartao = findViewById(R.id.etNomeCartao)
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

                    // Adiciona o cartao com as informacoes obtidas acima
                    registerCard(card, currentUser.uid)
                        .addOnCompleteListener { task ->
                            val e = task.exception
                            if (e != null) {
                                if (e.message == "UNKNOWN") {
                                    // Exibe mensagem de sucesso
                                    showToast("Cartão adicionado")
                                } else {
                                    // Exibe mensagem de erro
                                    showToast("Erro ao adicionar cartão.")
                                }
                            }
                        }
                } else {
                    // Mensagem de campos nao preenchidos
                    showToast("Preencha todos os campos! Todos os campos são obrigatórios.")
                }
            } else {
                // Usuario nao esta autenticado!!
                showToast("Usuário não autenticado! Por favor, faça login.")
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

    // Adiciona o cartao no Firebase
    private fun registerCard(card: Card, uid: String): Task<String> {
        val data = hashMapOf(
            "cardnumber" to card.numeroCartao,
            "cardname" to card.nomeTitular,
            "carddate" to card.dataExpiracao,
            "cardcvv" to card.cvv,
            "uid" to uid
        )

        // Envia como data para a funcao para cadastrar o cartao
        return functions
            .getHttpsCallable("funcCadastrarCartao")
            .call(data)
            .continueWith { task ->
                val result: String = task.result?.data as String
                Log.d(TAG, result)
                result
            }
    }

    // Mensagem toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
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