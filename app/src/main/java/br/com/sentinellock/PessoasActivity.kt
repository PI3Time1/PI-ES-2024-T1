package br.com.sentinellock

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator

class PessoasActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pessoas)

        val textView: TextView = findViewById(R.id.textViewPessoas)

        // Obtém os dados do QR code da intent
        val qrCodeData = intent.getStringExtra("qrcode_data")

        // Exibe os dados do QR code no TextView
        textView.text = "Dados do QR Code: $qrCodeData"
    }

    override fun onPause() {
        super.onPause()
        // Cancela a leitura do QR code quando a atividade estiver pausada
        IntentIntegrator(this)
    }
}
