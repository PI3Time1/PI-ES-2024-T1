// Pacote onde a classe TelaArmarioActivity está localizada
package br.com.sentinellock

// Importações necessárias para funcionalidades do Android
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

// Classe TelaArmarioActivity, que representa a tela de armários
class TelaArmarioActivity : AppCompatActivity() {

    // Código de solicitação de permissão de localização
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    // ID do item selecionado na BottomNavigationView
    private var selectedItemId: Int = R.id.action_look

    // Listener para os itens da BottomNavigationView
    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_map -> {
                    // Lidar com ação do mapa
                    return@OnNavigationItemSelectedListener true
                }
                R.id.action_look -> {
                    // Lidar com ação de busca
                    return@OnNavigationItemSelectedListener true
                }
                R.id.action_profile -> {
                    // Lidar com ação do perfil
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    // Método chamado quando a atividade é criada
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Log para indicar o início do método onCreate
        Log.d("TelaArmarioActivity", "onCreate() iniciado")

        // Define o layout da atividade
        setContentView(R.layout.activity_tela_armario2)

        // Obtém uma referência para o botão "Alugar armário" no layout
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

        // Obtém uma referência para a BottomNavigationView no layout
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        // Define o listener para os itens da BottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        // Define o item selecionado na BottomNavigationView
        bottomNavigationView.selectedItemId = selectedItemId

        // Configuração do listener para os itens da BottomNavigationView
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
                    // Inicia a ProfileActivity
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

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

    // Método para verificar se a permissão de localização foi concedida
    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Método para solicitar permissão de localização
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    // Método para iniciar a atividade de mapa
    private fun startMapsActivity() {
        val intent = Intent(this, MapsActivity2::class.java)
        startActivity(intent)
    }

    // Método chamado quando a atividade é pausada
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Salva o ID do item selecionado na BottomNavigationView
        outState.putInt("selectedItemId", selectedItemId)
    }

    // Método chamado quando a permissão é solicitada pelo usuário e uma resposta é recebida
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array <out String>,
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

