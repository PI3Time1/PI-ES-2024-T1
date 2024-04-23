package br.com.sentinellock

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TelaQrcodeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_qrcode2)

        // Recebendo informações de preço e tempo da tela anterior
        val preco = intent.getIntExtra("preco", 0)
        val tempo = intent.getIntExtra("tempo", 0)

        // Atualizando o texto do TextView com as informações recebidas
        if(tempo > 4){
            val textView: TextView = findViewById(R.id.textView)
            textView.text = "Preço: R$$preco, Tempo: $tempo Minutos"
        }else{
            val textView: TextView = findViewById(R.id.textView)
            textView.text = "Preço: R$$preco, Tempo: $tempo Horas"
        }

    }
}
