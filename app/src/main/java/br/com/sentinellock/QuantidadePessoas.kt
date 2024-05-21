package br.com.sentinellock

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import br.com.sentinellock.databinding.ActivityQuantidadePessoasBinding
import com.google.android.material.snackbar.Snackbar

class QuantidadePessoas : AppCompatActivity() {

    private lateinit var binding: ActivityQuantidadePessoasBinding
    private var quantidadePessoas = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityQuantidadePessoasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.botaoUmaPessoa.setOnClickListener{
            quantidadePessoas = 1
            cameraProviderResult.launch(Manifest.permission.CAMERA)
        }

        binding.botaoDuasPessoas.setOnClickListener{
            quantidadePessoas = 2
            cameraProviderResult.launch(Manifest.permission.CAMERA)
        }

//        binding.buttonBack.setOnClickListener {
//            startActivity(Intent(this, ProfileActivity::class.java))
//            finish()
//        }
    }

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
        }
        startActivity(intentCameraPreview)
    }
}
