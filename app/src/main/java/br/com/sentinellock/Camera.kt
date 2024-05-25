package br.com.sentinellock

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import br.com.sentinellock.databinding.ActivityCameraBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Camera : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector: CameraSelector
    private lateinit var buttonBack : Button
    private var imageCapture: ImageCapture? = null
    private lateinit var imgCaptureExecutor: ExecutorService
    private var quantidadePessoas = 0
    private var fotosTiradas = 0
    private val imageFilePaths = mutableListOf<String>()
    private lateinit var userId: String
    private lateinit var lockerId: String
    private var price: Double = 0.0
    private var duration: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recebendo os dados da tela anterior
        quantidadePessoas = intent.getIntExtra("QUANTIDADE_PESSOAS", 0)
        userId = intent.getStringExtra("userId") ?: ""
        lockerId = intent.getStringExtra("lockerId") ?: ""
        price = intent.getDoubleExtra("price", 0.0)
        duration = intent.getLongExtra("duration", 0)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        imgCaptureExecutor = Executors.newSingleThreadExecutor()

        startCamera()

        binding.buttonBack.setOnClickListener {
            showConfirmationDialog()
        }

        binding.botaoTirarFoto.setOnClickListener {
            tirarFoto()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flash()
            }
        }

        binding.botaoSegundaFoto.setOnClickListener {
            tirarFoto()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flash()
            }
        }

        if (quantidadePessoas == 2) {
            binding.botaoSegundaFoto.visibility = View.VISIBLE
        } else {
            binding.botaoSegundaFoto.visibility = View.GONE
        }
    }

    private fun startCamera() {
        cameraProviderFuture.addListener({
            imageCapture = ImageCapture.Builder().build()

            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.Camera.surfaceProvider)
            }
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

            } catch (e: Exception) {
                Log.e("CameraPreview", "Falha ao abrir a câmera!")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun tirarFoto() {
        imageCapture?.let {
            val fileName = "FOTO_JPEG_${System.currentTimeMillis()}.jpeg"
            val file = File(externalMediaDirs[0], fileName)
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()

            it.takePicture(
                outputFileOptions,
                imgCaptureExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Log.i("Camera", "A imagem foi salva no diretório: ${file.toURI()}")
                        imageFilePaths.add(file.absolutePath)
                        fotosTiradas++
                        if (fotosTiradas >= quantidadePessoas) {
                            // Se todas as fotos foram tiradas, passe as imagens para a próxima activity
                            val intent = Intent(this@Camera, RegisterNFCActivity::class.java)
                            intent.putStringArrayListExtra("IMAGE_PATHS", ArrayList(imageFilePaths))
                            // Passando os dados para a próxima tela
                            intent.putExtra("QUANTIDADE_PESSOAS", quantidadePessoas)
                            intent.putExtra("userId", userId)
                            intent.putExtra("lockerId", lockerId)
                            intent.putExtra("price", price)
                            intent.putExtra("duration", duration)
                            startActivity(intent)
                            finish()
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(binding.root.context, "Erro ao salvar foto", Toast.LENGTH_LONG).show()
                        Log.e("Camera", "Exceção ao gravar arquivo da foto: $exception")
                    }
                }
            )
        }
    }

    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmação")
        builder.setMessage("Tem certeza que deseja voltar? Todos os dados não salvos serão perdidos.")
        builder.setPositiveButton("Sim") { _, _ ->
            // Atualizar status no Firebase
            updateStatusOnFirebase()
            // Iniciar a atividade MenuGerente
            startActivity(Intent(this, MenuGerente::class.java))
            finish()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }


    private fun updateStatusOnFirebase() {
        val db = FirebaseFirestore.getInstance()
        db.collection("unidade_de_locacao").document(lockerId)
            .update(
                mapOf(
                    "aberto" to true,
                    "status" to true
                )
            )
            .addOnSuccessListener {
                Toast.makeText(this, "Status atualizado no Firebase.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao atualizar status no Firebase: $e", Toast.LENGTH_SHORT).show()
            }
    }



    @RequiresApi(Build.VERSION_CODES.M)
    private fun flash() {
        binding.root.postDelayed({
            binding.root.foreground = ColorDrawable(Color.WHITE)
            binding.root.postDelayed({
                binding.root.foreground = null
            }, 50)
        }, 100)
    }
}
