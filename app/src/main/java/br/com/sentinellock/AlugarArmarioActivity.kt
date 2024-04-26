package br.com.sentinellock

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*

class AlugarArmarioActivity : AppCompatActivity() {

    // Firebase
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Variáveis ​​de seleção de preço e tempo
    private var selectedPrice: Int = 0
    private var selectedTime: Int = 0
    private var selectedButtonId: Int = 0

    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_map -> {
                    // Handle map action
                    return@OnNavigationItemSelectedListener true
                }
                R.id.action_look -> {
                    // Handle search action
                    return@OnNavigationItemSelectedListener true
                }
                R.id.action_profile -> {
                    // Handle profile action
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    // ID do item selecionado no Navigation Bottom
    private var selectedItemId: Int = R.id.action_look

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alugar_armario2)

        // Recuperar o ID do item selecionado, se disponível
        savedInstanceState?.getInt("selectedItemId")?.let {
            selectedItemId = it
        }

        // Configurar a Bottom Navigation View
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        bottomNavigationView.selectedItemId = selectedItemId

        // Configurar a ação para os itens do Navigation Bottom
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_look -> {
                    startActivity(Intent(this, TelaArmarioActivity::class.java))
                    true
                }
                R.id.action_map -> {
                    startActivity(Intent(this, MapsActivity2::class.java))
                    true
                }
                else -> false
            }
        }

        // Obter dados passados da tela anterior
        val nome = intent.getStringExtra("nome")
        val precoMeiaHora = intent.getIntExtra("precoMeiaHora", 0)
        val precoUmaHora = intent.getIntExtra("precoUmaHora", 0)
        val precoDuasHoras = intent.getIntExtra("precoDuasHoras", 0)
        val precoQuatroHoras = intent.getIntExtra("precoQuatroHoras", 0)
        val promocao = intent.getIntExtra("promocao", 0)

        // Exibir o nome do armário na TextView
        val textView: TextView = findViewById(R.id.textView)
        textView.text = nome

        // Configurar os botões de seleção de tempo
        val button1: Button = findViewById(R.id.button1)
        button1.text = "30 min                   R$$precoMeiaHora"
        button1.setOnClickListener {
            deselectButton(selectedButtonId)
            selectButton(it.id)
            selectedPrice = precoMeiaHora
            selectedTime = 30
            findViewById<Button>(R.id.button5)?.visibility = View.VISIBLE
        }

        val button2: Button = findViewById(R.id.button2)
        button2.text = "1 hora                   R$$precoUmaHora"
        button2.setOnClickListener {
            deselectButton(selectedButtonId)
            selectButton(it.id)
            selectedPrice = precoUmaHora
            selectedTime = 1
            findViewById<Button>(R.id.button5)?.visibility = View.VISIBLE
        }

        val button3: Button = findViewById(R.id.button3)
        button3.text = "2 horas               R$$precoDuasHoras"
        button3.setOnClickListener {
            deselectButton(selectedButtonId)
            selectButton(it.id)
            selectedPrice = precoDuasHoras
            selectedTime = 2
            findViewById<Button>(R.id.button5)?.visibility = View.VISIBLE
        }

        val button4: Button = findViewById(R.id.button4)
        button4.text = "4 horas               R$$precoQuatroHoras"
        button4.setOnClickListener {
            deselectButton(selectedButtonId)
            selectButton(it.id)
            selectedPrice = precoQuatroHoras
            selectedTime = 4
            findViewById<Button>(R.id.button5)?.visibility = View.VISIBLE
        }

        // Verificar se está entre 07:00 e 08:00 horas
        val currentTime = Calendar.getInstance().time
        val calendar = Calendar.getInstance()
        calendar.time = currentTime
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        if (hour >= 7 && hour < 8) {
            val buttonPromocao: Button = findViewById(R.id.buttonPromocao)
            buttonPromocao.text = "Promoção do dia!"
            buttonPromocao.visibility = View.VISIBLE
            buttonPromocao.setOnClickListener {
                deselectButton(selectedButtonId)
                selectButton(it.id)
                selectedPrice = promocao
                selectedTime = 16
            }
        }

        // Configurar o botão para confirmar a locação
        val button5: Button = findViewById(R.id.button5)
        button5.setOnClickListener {
            // Verificar se o usuário possui cartões cadastrados
            val userId = firebaseAuth.currentUser?.uid
            userId?.let { uid ->
                firestore.collection("pessoas").document(uid).get()
                    .addOnSuccessListener { document ->
                        val cartoes = document?.get("cartaoCredito") as? Map<*, *>
                        if (cartoes.isNullOrEmpty()) {
                            // Se não houver cartões, exibir mensagem
                            showMessage("Você precisa cadastrar um cartão para continuar.")
                        } else {
                            // Se houver cartões, prosseguir para a próxima tela
                            val intent = Intent(this, TelaQrcodeActivity::class.java)
                            intent.putExtra("preco", selectedPrice)
                            intent.putExtra("tempo", selectedTime)
                            startActivity(intent)
                        }
                    }
                    .addOnFailureListener { e ->
                        showMessage("Você precisa cadastrar um cartão para continuar.")
                    }
            }
        }
    }

    // Método para exibir uma mensagem na tela
    private fun showMessage(message: String) {
        // Criar um AlertDialog
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage(message)

        // Adicionar um botão para fechar o dialog
        alertDialogBuilder.setPositiveButton("Fechar") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss() // Fechar o dialog
        }

        // Criar e exibir o AlertDialog
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    // Funções auxiliares para seleção de botões
    private fun selectButton(buttonId: Int) {
        val button = findViewById<Button>(buttonId)
        button?.apply {
            backgroundTintList = ContextCompat.getColorStateList(this@AlugarArmarioActivity, R.color.buttom_green)
            setTextColor(resources.getColor(R.color.white))
        }
        selectedButtonId = buttonId
    }

    private fun deselectButton(buttonId: Int) {
        val button = findViewById<Button>(buttonId)
        button?.apply {
            backgroundTintList = ContextCompat.getColorStateList(this@AlugarArmarioActivity, android.R.color.white)
            setTextColor(resources.getColor(R.color.black))
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("selectedItemId", selectedItemId)
    }
}