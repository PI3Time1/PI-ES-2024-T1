// Pacote onde a classe TelaQrcodeActivity está localizada
package br.com.sentinellock

// Importações necessárias para funcionalidades do Android
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.util.Locale

// Classe TelaQrcodeActivity, que representa a tela de exibição do QR code
class TelaQrcodeActivity : AppCompatActivity() {

    // Variáveis de estado e controle
    private lateinit var buttonSearch : Button
    private var preco: Int = 0
    private var tempo: Int = 0
    private var qrCodeData: String = ""
    private var isAppInForeground = true
    private var isConfirmationDialogShown = false

    // Método chamado quando a atividade é criada
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Define o layout da atividade
        setContentView(R.layout.activity_tela_qrcode2)

        // Obtém os valores de preço e tempo do Intent ou do estado salvo
        preco = savedInstanceState?.getInt("preco") ?: intent.getIntExtra("preco", 0)
        tempo = savedInstanceState?.getInt("tempo") ?: intent.getIntExtra("tempo", 0)

        // Obtém uma referência para o TextView no layout
        val textView: TextView = findViewById(R.id.textView)
        buttonSearch = findViewById(R.id.buttonSearch)

        buttonSearch.setOnClickListener {
            // Crie uma Intent para iniciar a TelaArmarioActivity
            val intent = Intent(this, TelaArmarioActivity::class.java)
            startActivity(intent)
            finish()
        }
        // Formata o texto com base nos valores de preço e tempo
        val tempoText = if (tempo > 16) {
            String.format(Locale.getDefault(), "%d Minutos", tempo)
        } else {
            String.format(Locale.getDefault(), "%d Horas", tempo)
        }
        textView.text = String.format(Locale.getDefault(), "Preço: R$%d, Tempo: %s", preco, tempoText)

        // Cria os dados do QR code
        qrCodeData = "Preço: ${preco}, Tempo: ${tempo}"
        // Gera o bitmap do QR code
        val qrCodeBitmap = generateQRCode(qrCodeData)

        // Obtém uma referência para o SurfaceView no layout
        val qrCodeSurfaceView: SurfaceView = findViewById(R.id.qrCodeScannerView)
        val holder = qrCodeSurfaceView.holder
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
                val canvas = surfaceHolder.lockCanvas()
                canvas.drawBitmap(qrCodeBitmap, 0f, 0f, null)
                surfaceHolder.unlockCanvasAndPost(canvas)
            }

            override fun surfaceChanged(surfaceHolder: SurfaceHolder, format: Int, width: Int, height: Int) {}

            override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {}
        })
    }

    // Método chamado quando a atividade é retomada
    override fun onResume() {
        super.onResume()
        isAppInForeground = true
        if (!isAppInForeground && isConfirmationDialogShown) {
            showConfirmationDialog()
        }
        isAppInForeground = true
    }

    // Método chamado quando a atividade é pausada
    override fun onPause() {
        super.onPause()
        isAppInForeground = false
    }

    // Método chamado quando a atividade é parada
    override fun onStop() {
        super.onStop()
        if (!isAppInForeground) {
            showConfirmationDialog()
        }
    }

    // Método chamado para salvar o estado da atividade
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("preco", preco)
        outState.putInt("tempo", tempo)
        super.onSaveInstanceState(outState)
    }

    // Método para gerar o bitmap do QR code
    private fun generateQRCode(data: String): Bitmap {
        val barcodeEncoder = BarcodeEncoder()
        return try {
            barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 750, 750)
        } catch (e: WriterException) {
            e.printStackTrace()
            Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888)
        }
    }

    // Método para exibir o diálogo de confirmação
    private fun showConfirmationDialog() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Continuar compra?")
        alertDialog.setMessage("Deseja continuar a compra com as informações anteriores?")
        alertDialog.setPositiveButton("Sim") { dialogInterface: DialogInterface, _: Int ->
            // Continuar com a compra, não precisa fazer nada aqui
            dialogInterface.dismiss()
        }
        alertDialog.setNegativeButton("Cancelar") { dialogInterface: DialogInterface, _: Int ->
            // Cancelar a compra e voltar para a tela anterior
            dialogInterface.dismiss()
            val intent = Intent(this, TelaArmarioActivity::class.java)
            startActivity(intent)
            finish()
        }
        alertDialog.setCancelable(false) // Impede que o usuário clique fora do dialog para fechá-lo
        alertDialog.setOnDismissListener {
            isConfirmationDialogShown = false
        }
        alertDialog.show()
        isConfirmationDialogShown = true
    }
}
