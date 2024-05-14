package br.com.sentinellock

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

object BitmapHelper {

    // Função para converter um vetor em um bitmap com a cor especificada
    fun vectorToBitmap(
        context: Context, // Contexto da aplicação
        @DrawableRes id: Int, // ID do recurso do vetor
        @ColorInt color: Int // Cor do ícone do vetor
    ) : BitmapDescriptor { // Retorna um BitmapDescriptor usado para personalizar marcadores no Google Maps

        // Obtém o vetor drawable com o ID fornecido
        val vectorDrawable = ResourcesCompat.getDrawable(context.resources,id,null)

        // Verifica se o vetor drawable não é nulo
        if(vectorDrawable == null){
            // Se for nulo, retorna um marcador de mapa padrão
            return BitmapDescriptorFactory.defaultMarker()
        }

        // Cria um bitmap com as dimensões do vetor
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        // Cria um canvas para desenhar o vetor no bitmap
        val canvas = Canvas(bitmap)
        // Define os limites do vetor no canvas
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height);
        // Desenha o vetor no canvas
        vectorDrawable.draw(canvas)

        // Retorna um BitmapDescriptor criado a partir do bitmap, usado para personalizar marcadores no mapa
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
