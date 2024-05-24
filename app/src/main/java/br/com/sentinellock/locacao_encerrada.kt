package br.com.sentinellock

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class locacao_encerrada : AppCompatActivity() {

    private lateinit var buttonBack : Button
    private lateinit var buttonBackToMenu : Button
    override fun onCreate(savedInstanceState: Bundle?) {

        buttonBack = findViewById(R.id.buttonBack)
        buttonBackToMenu = findViewById(R.id.buttonBackToMenu)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locacao_encerrada)

        buttonBack.setOnClickListener {
            val intent = Intent(this,MenuGerente::class.java)
            startActivity(intent)
            finish()
        }
        buttonBackToMenu.setOnClickListener {
            val intent = Intent(this,MenuGerente::class.java)
            startActivity(intent)
            finish()
        }

    }
}