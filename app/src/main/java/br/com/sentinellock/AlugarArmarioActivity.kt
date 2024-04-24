package br.com.sentinellock


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class AlugarArmarioActivity : AppCompatActivity() {

    private var selectedPrice: Int = 0
    private var selectedTime: Int = 0

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
        setContentView(R.layout.activity_alugar_armario2)

//        savedInstanceState?.getInt("selectedItemId")?.let {
//            selectedItemId = it
//        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        bottomNavigationView.selectedItemId = selectedItemId

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

        val nome = intent.getStringExtra("nome")
        val precoMeiaHora = intent.getIntExtra("precoMeiaHora", 0)
        val precoUmaHora = intent.getIntExtra("precoUmaHora", 0)
        val precoDuasHoras = intent.getIntExtra("precoDuasHoras", 0)
        val precoQuatroHoras = intent.getIntExtra("precoQuatroHoras", 0)
        val promocao = intent.getIntExtra("promocao", 0)

        val textView: TextView = findViewById(R.id.textView)
        textView.text = nome

        val button1: Button = findViewById(R.id.button1)
        button1.text = "30 min                   R$$precoMeiaHora"
        button1.setOnClickListener {
            selectedPrice = precoMeiaHora
            selectedTime = 30
            findViewById<Button>(R.id.button5)?.visibility = View.VISIBLE
        }

        val button2: Button = findViewById(R.id.button2)
        button2.text = "1 hora                   R$$precoUmaHora"
        button2.setOnClickListener {
            selectedPrice = precoUmaHora
            selectedTime = 1
            findViewById<Button>(R.id.button5)?.visibility = View.VISIBLE
        }

        val button3: Button = findViewById(R.id.button3)
        button3.text = "2 horas               R$$precoDuasHoras"
        button3.setOnClickListener {
            selectedPrice = precoDuasHoras
            selectedTime = 2
            findViewById<Button>(R.id.button5)?.visibility = View.VISIBLE
        }

        val button4: Button = findViewById(R.id.button4)
        button4.text = "4 horas               R$$precoQuatroHoras"
        button4.setOnClickListener {
            selectedPrice = precoQuatroHoras
            selectedTime = 4
            findViewById<Button>(R.id.button5)?.visibility = View.VISIBLE
        }

        val button5: Button = findViewById(R.id.button5)
        button5.setOnClickListener {
            val intent = Intent(this, TelaQrcodeActivity::class.java)
            intent.putExtra("preco", selectedPrice)
            intent.putExtra("tempo", selectedTime)
            startActivity(intent)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("selectedItemId", selectedItemId)
    }
}
