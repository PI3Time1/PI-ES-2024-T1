package br.com.sentinellock

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import br.com.sentinellock.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class TelaArmarioActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private var selectedItemId: Int = R.id.action_look

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TelaArmarioActivity", "onCreate() iniciado")
        enableEdgeToEdge()
        setContentView(R.layout.activity_tela_armario2)

        val buttonAlugarArmario: Button = findViewById(R.id.button4)

        // Adicionando um OnClickListener ao botão "Alugar armário"
        buttonAlugarArmario.setOnClickListener {
            // Verifica permissão de localização antes de iniciar a atividade de mapa
            if (checkLocationPermission()) {
                startMapsActivity()
            } else {
                requestLocationPermission()
            }
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
                    // Inicia a TelaArmarioActivity
                    startActivity(Intent(this, TelaArmarioActivity::class.java))
                    true
                }
                R.id.action_map -> {
                    // Verifica permissão de localização antes de iniciar a atividade de mapa
                    if (checkLocationPermission()) {
                        startMapsActivity()
                    } else {
                        requestLocationPermission()
                    }
                    true
                }
                R.id.action_profile -> {
                    // Não faz nada se a opção "Armário" já estiver selecionada
                    if (menuItem.itemId == bottomNavigationView.selectedItemId) {
                        return@setOnNavigationItemSelectedListener true
                    }
                    // Inicia a TelaArmarioActivity
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Verifica se há uma compra pendente ao reabrir o aplicativo
        // Verifica se há uma compra pendente ao reabrir o aplicativo
        if (savedInstanceState == null) {
            val sharedPreferences = getSharedPreferences("Compra", MODE_PRIVATE)
            val compraPendente = sharedPreferences.getBoolean("compraPendente", false)
            Log.d("TelaArmarioActivity", "Compra pendente: $compraPendente")
            if (compraPendente) {
                // Se houver compra pendente, exibe uma mensagem
                Log.d("TelaArmarioActivity", "Há uma compra pendente")
                Toast.makeText(this, "Há uma compra pendente", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun startMapsActivity() {
        val intent = Intent(this, MapsActivity2::class.java)
        startActivity(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("selectedItemId", selectedItemId)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissão concedida, inicia a atividade de mapa
                startMapsActivity()
            } else {
                // Permissão negada, você pode mostrar uma mensagem informando ao usuário
                // ou solicitar novamente a permissão.
            }
        }
    }
}
