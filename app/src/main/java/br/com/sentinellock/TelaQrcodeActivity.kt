package br.com.sentinellock

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_qrcode2)

        val preco = intent.getIntExtra("preco", 0)
        val tempo = intent.getIntExtra("tempo", 0)

        val textView: TextView = findViewById(R.id.textView)

        // Atualizando o texto do TextView com as informações recebidas
        val tempoText = if (tempo > 4) {
            String.format(Locale.getDefault(), "%d Minutos", tempo)
        } else {
            String.format(Locale.getDefault(), "%d Horas", tempo)
        }
        textView.text = String.format(Locale.getDefault(), "Preço: R$%d, Tempo: %s", preco, tempoText)

        // Gerando o QR code com as informações de preço e tempo
        val qrCodeData = String.format(Locale.getDefault(), "Preço: R$%d, Tempo: %d", preco, tempo)
        val qrCodeBitmap = generateQRCode(qrCodeData)

        // Exibindo o QR code na SurfaceView
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

    private fun generateQRCode(data: String): Bitmap {
        val barcodeEncoder = BarcodeEncoder()
        return try {
            val bitMatrix = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 750, 750)
            bitMatrix
        } catch (e: WriterException) {
            e.printStackTrace()
            Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888)
        }
    }
}
