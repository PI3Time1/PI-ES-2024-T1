package br.com.sentinellock

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CardsActivity : AppCompatActivity() {

    private lateinit var nomeTitularView: TextView
    private lateinit var finalCartaoView: TextView
    private lateinit var buttonWantAddCard: Button
    private lateinit var cardCardView: MaterialCardView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gerenciar_cartoes)

        nomeTitularView = findViewById(R.id.nomeTitularView)
        finalCartaoView = findViewById(R.id.finalCartaoView)
        buttonWantAddCard = findViewById(R.id.buttonWantAddCard)
        cardCardView = findViewById(R.id.cardCardView)

        // Verifica se há cartões cadastrados no Firestore
        verificarCartoes()

        // Configuração do botão para redirecionar para a AddCardActivity
        buttonWantAddCard.setOnClickListener {
            startActivity(Intent(this, AddCardActivity::class.java))
        }
    }

    private fun verificarCartoes() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val docRef = db.collection("pessoas").document(userId)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val cartoes = document.get("cartoes") as? Map<String, Any>
                        if (!cartoes.isNullOrEmpty()) {
                            // Se houver cartões cadastrados, mostra o cardCardView
                            cardCardView.visibility = View.VISIBLE
                            val cartao = cartoes.values.firstOrNull() as? Map<String, Any>
                            if (cartao != null) {
                                // Extraindo informações do cartão
                                val nomeTitular = cartao["nomeTitular"] as? String ?: ""
                                val numeroCartao = cartao["numeroCartao"] as? String ?: ""
                                // Mostrando apenas os últimos 4 dígitos do número do cartão
                                val finalCartao = numeroCartao.takeLast(4)
                                // Atualizando as views com as informações do cartão
                                nomeTitularView.text = nomeTitular
                                finalCartaoView.text = "*$finalCartao"
                            }
                        } else {
                            // Se não houver cartões cadastrados, oculta o cardCardView
                            cardCardView.visibility = View.GONE
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Erro ao acessar Firestore: ", exception)
                }
        }
    }
}
