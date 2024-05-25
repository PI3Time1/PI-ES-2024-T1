package br.com.sentinellock

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import br.com.sentinellock.databinding.ActivityQuantidadePessoasBinding
import com.google.android.material.snackbar.Snackbar

class QuantidadePessoas : AppCompatActivity() {
// inicialização das variáveis necessárias
    private lateinit var binding: ActivityQuantidadePessoasBinding
    private var quantidadePessoas = 0
    private var userId: String = ""
    private var lockerId: String = ""
    private var price: Double = 0.0
    private var duration: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityQuantidadePessoasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recebe os extras do intent
        userId = intent.getStringExtra("userId") ?: ""
        lockerId = intent.getStringExtra("lockerId") ?: ""
        price = intent.getDoubleExtra("price", 0.0)
        duration = intent.getLongExtra("duration", 0)
// botao que redireciona para a CAMERA e com 1 pessoa só
        binding.botaoUmaPessoa.setOnClickListener{
            quantidadePessoas = 1
            cameraProviderResult.launch(Manifest.permission.CAMERA)
        }
// botao que redireciona para a CAMERA e com 2 pessoas
        binding.botaoDuasPessoas.setOnClickListener{
            quantidadePessoas = 2
            cameraProviderResult.launch(Manifest.permission.CAMERA)
        }
    }
// permissão para abrir a camera/ se for liberado abre a tela e se não for uma mensagem atraves da snackbar aparece
    private val cameraProviderResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if(it){
                abrirTelaCAMERA()
            }else{
                Snackbar.make(binding.root, "Você não cedeu acesso a câmera.", Snackbar.LENGTH_INDEFINITE).show()
            }
        }

    private fun abrirTelaCAMERA(){
        val intentCameraPreview = Intent(this, Camera::class.java).apply {
            putExtra("QUANTIDADE_PESSOAS", quantidadePessoas)
            // Passa as informações para a próxima tela
            putExtra("userId", userId)
            putExtra("lockerId", lockerId)
            putExtra("price", price)
            putExtra("duration", duration)
        }
        startActivity(intentCameraPreview)
    }
}
