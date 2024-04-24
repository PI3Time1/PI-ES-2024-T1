package br.com.sentinellock

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import br.com.sentinellock.AlugarArmarioActivity
import br.com.sentinellock.MapsActivity2
import br.com.sentinellock.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class TelaArmarioActivity : AppCompatActivity() {

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
    private var selectedItemId: Int = R.id.action_look

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tela_armario2)

        val buttonAlugarArmario: Button = findViewById(R.id.button4)

        // Adicionando um OnClickListener ao botão "Alugar armário"
        buttonAlugarArmario.setOnClickListener {
            // Criando um intent para iniciar a AlugarArmarioActivity
            val intent = Intent(this, MapsActivity2::class.java)
            startActivity(intent)
        }


        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        bottomNavigationView.selectedItemId = selectedItemId



        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_look -> {
                    // Não faz nada se a opção "Armário" já estiver selecionada
                    if (menuItem.itemId == bottomNavigationView.selectedItemId) {
                        return@setOnNavigationItemSelectedListener true
                    }
                    // Inicia a AlugarArmarioActivity
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
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("selectedItemId", selectedItemId)
    }
}
