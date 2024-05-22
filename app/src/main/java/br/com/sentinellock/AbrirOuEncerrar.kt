package br.com.sentinellock

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AbrirOuEncerrar : AppCompatActivity() {

    private lateinit var buttonBack : Button
    private lateinit var AbrirMomentaneamente : Button
    private lateinit var EncerrarLocacao : Button
    override fun onCreate(savedInstanceState: Bundle?) {

        buttonBack = findViewById(R.id.buttonBack)
        AbrirMomentaneamente = findViewById(R.id.AbrirMomentaneamente)
        EncerrarLocacao = findViewById(R.id.EncerrarLocacao)


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_abrir_ou_encerrar)

        buttonBack.setOnClickListener {
            val intent = Intent(this,ReadNFCActivity::class.java)
            startActivity(intent)
            finish()
        }
//        buttonBack.setOnClickListener {
//            val intent = Intent(this,//ArmarioLiberado::class.java)
//            startActivity(intent)
//            finish()
//        }
//        buttonBack.setOnClickListener {
//            val intent = Intent(this,//LocacaoEncerrada::class.java)
//            startActivity(intent)
//            finish()
//        }
    }
}