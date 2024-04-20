package br.com.sentinellock

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.widget.TextView
import com.google.android.gms.location.places.Place
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class MarkerInfoAdapter(private val context: Context):GoogleMap.InfoWindowAdapter {
    override fun getInfoContents(marker: Marker): View? = null
    @SuppressLint("MissingInflatedId")
    override fun getInfoWindow(marker: Marker): View? {
        val place =  marker.tag as? Place ?: return null

        val viwe = LayoutInflater.from(context).inflate(R.layout.custom_marker_info, null)

        viwe.findViewById<TextView>(R.id.text_title).text = place.name
        viwe.findViewById<TextView>(R.id.text_address).text = place.address
//        viwe.findViewById<TextView>(R.id.text_rating).text = context.getString(R.string.rating)

        return viwe
    }

}