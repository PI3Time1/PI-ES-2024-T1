package br.com.sentinellock

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

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
}
