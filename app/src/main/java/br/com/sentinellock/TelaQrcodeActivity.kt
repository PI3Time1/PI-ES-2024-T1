package br.com.sentinellock

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.util.Locale

class TelaQrcodeActivity : AppCompatActivity() {

    private var preco: Int = 0
    private var tempo: Int = 0
    private var qrCodeData: String = ""
    private var isAppInForeground = true
    private var isConfirmationDialogShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_qrcode2)

        preco = savedInstanceState?.getInt("preco") ?: intent.getIntExtra("preco", 0)
        tempo = savedInstanceState?.getInt("tempo") ?: intent.getIntExtra("tempo", 0)

        val textView: TextView = findViewById(R.id.textView)

        val tempoText = if (tempo > 16) {
            String.format(Locale.getDefault(), "%d Minutos", tempo)
        } else {
            String.format(Locale.getDefault(), "%d Horas", tempo)
        }
        textView.text = String.format(Locale.getDefault(), "Preço: R$%d, Tempo: %s", preco, tempoText)

        qrCodeData = "Preço: ${preco}, Tempo: ${tempo}"
        val qrCodeBitmap = generateQRCode(qrCodeData)

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

    override fun onResume() {
        super.onResume()
        isAppInForeground = true
        if (!isAppInForeground && isConfirmationDialogShown) {
            showConfirmationDialog()
        }
        isAppInForeground = true
    }

    override fun onPause() {
        super.onPause()
        isAppInForeground = false
    }

    override fun onStop() {
        super.onStop()
        if (!isAppInForeground) {
            showConfirmationDialog()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("preco", preco)
        outState.putInt("tempo", tempo)
        super.onSaveInstanceState(outState)
    }

    private fun generateQRCode(data: String): Bitmap {
        val barcodeEncoder = BarcodeEncoder()
        return try {
            barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 750, 750)
        } catch (e: WriterException) {
            e.printStackTrace()
            Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888)
        }
    }

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
