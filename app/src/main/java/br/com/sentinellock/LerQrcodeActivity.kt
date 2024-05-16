package br.com.sentinellock

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class LerQrcodeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ler_qrcode)

        // Inicia o scanner de QR code
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE) // Define para ler apenas QR codes
        integrator.setPrompt("Aponte a câmera para o QR Code") // Mensagem para o usuário
        integrator.setCameraId(0) // Use a câmera traseira
        integrator.setBeepEnabled(true) // Habilita som ao ler QR code
        integrator.setBarcodeImageEnabled(true) // Salva uma imagem do QR code lido
        integrator.initiateScan()
    } 

    // Método que lida com o resultado do scanner de QR code
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                // Exibe as informações lidas do QR code em um Toast
                Toast.makeText(this, "Dados lidos: ${result.contents}", Toast.LENGTH_LONG).show()

                // QR code foi lido com sucesso
                val intent = Intent(this, PessoasActivity::class.java)
                intent.putExtra("qrcode_data", result.contents)
                startActivity(intent)
                finish()
            } else {
                // Falha na leitura do QR code
                Toast.makeText(this, "Falha na leitura do QR code", Toast.LENGTH_LONG).show()
                finish()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
