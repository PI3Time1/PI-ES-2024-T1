package br.com.sentinellock

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import br.com.sentinellock.RegisterActivity.Companion.TAG
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CardsActivity : AppCompatActivity() {
    private lateinit var buttonBack: Button
    private lateinit var nomeTitularView: TextView
    private lateinit var finalCartaoView: TextView
    private lateinit var buttonWantAddCard: Button
    private lateinit var cardCardView: MaterialCardView

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gerenciar_cartoes)

        buttonBack = findViewById(R.id.buttonBack)
        nomeTitularView = findViewById(R.id.nomeTitularView)
        finalCartaoView = findViewById(R.id.finalCartaoView)
        buttonWantAddCard = findViewById(R.id.buttonWantAddCard)
        cardCardView = findViewById(R.id.cardCardView)

        buttonBack.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }

        // Verifica se há cartão de crédito cadastrado para o usuário atual
        verificarCartaoCredito()

        // Configuração do botão para redirecionar para a AddCardActivity
        buttonWantAddCard.setOnClickListener {
            val intent = Intent(this, AddCardActivity::class.java)
            startActivity(intent)
        }
    }

    private fun verificarCartaoCredito() {
        userId?.let { userId ->
            val docRef = db.collection("pessoas").document(userId)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val cartaoCredito = document.get("cartaoCredito") as? Map<*, *>
                        if (cartaoCredito != null) {
                            // Se o campo cartaoCredito for um mapa, tente extrair os dados
                            val nomeTitular = cartaoCredito["nomeTitular"] as? String ?: ""
                            val numeroCartao = cartaoCredito["numeroCartao"] as? String ?: ""

                            // Mostrando apenas os últimos 4 dígitos do número do cartão
                            val finalCartao = numeroCartao.takeLast(4)

                            // Atualizando as views com as informações do cartão
                            nomeTitularView.text = nomeTitular
                            finalCartaoView.text = "*$finalCartao"

                            // Mostra o cardCardView se houver um cartão de crédito cadastrado
                            cardCardView.visibility = View.VISIBLE

                            // Oculta o botão de adicionar cartão
                            buttonWantAddCard.visibility = View.GONE
                        } else {
                            // Se cartaoCredito não for um mapa válido, oculta o cardCardView
                            cardCardView.visibility = View.GONE

                            // Mostra o botão de adicionar cartão
                            buttonWantAddCard.visibility = View.VISIBLE
                        }
                    } else {
                        // Se o documento "pessoas" não existir para o usuário atual, oculta o cardCardView
                        cardCardView.visibility = View.GONE

                        // Mostra o botão de adicionar cartão
                        buttonWantAddCard.visibility = View.VISIBLE
                    }
                }
                .addOnFailureListener { exception ->
                    // Log de erro ao acessar o Firestore
                    Log.e(TAG, "Erro ao acessar Firestore: ", exception)
                    // Oculta o cardCardView em caso de falha
                    cardCardView.visibility = View.GONE

                    // Mostra o botão de adicionar cartão em caso de falha
                    buttonWantAddCard.visibility = View.VISIBLE
                }
        }
    }
}