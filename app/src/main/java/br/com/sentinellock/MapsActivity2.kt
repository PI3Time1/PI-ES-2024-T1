package br.com.sentinellock

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import br.com.sentinellock.R.string.google_maps_key
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomnavigation.BottomNavigationView

class MapsActivity2 : AppCompatActivity(), OnMapReadyCallback {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mMap: GoogleMap? = null
    private var currentLocationMarker: Marker? = null
    private var currentPolyline: Polyline? = null

    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_map -> {
                    // Handle map action
                    return@OnNavigationItemSelectedListener true
                }
                R.id.action_search -> {
                    // Handle search action
                    return@OnNavigationItemSelectedListener true
                }
                R.id.action_profile -> {
                    // Handle profile action
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }


    private val places = arrayListOf(
        Places("PUC", LatLng(-22.834445, -47.1881626), "PUC CAMPIANS CAMPUS 1", 4.8f, R.drawable.puc_image),
        Places("JARDIM", LatLng(-22.830332, -47.068686), "JARDIM", 4.9f, R.drawable.jardim_image)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps2)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        enableMyLocation()
        addMarkers()

        mMap?.setOnMarkerClickListener { marker ->
            val place = marker.tag as? Places
            place?.let {
                showBottomDialog(it)
            }
            true
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap?.isMyLocationEnabled = true
            moveCameraToCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun moveCameraToCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    addOrUpdateCurrentLocationMarker(currentLatLng)
                }
            }
        }
    }

    private fun addOrUpdateCurrentLocationMarker(latLng: LatLng) {
        if (currentLocationMarker == null) {
            currentLocationMarker = mMap?.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
        } else {
            currentLocationMarker?.position = latLng
        }
    }

    private fun addMarkers() {
        places.forEach { place ->
            val marker = mMap?.addMarker(
                MarkerOptions()
                    .title(place.name)
                    .snippet(place.address)
                    .position(place.latLng)
                    .icon(BitmapHelper.vectorToBitmap(this, R.drawable.maps_logo_vetor, ContextCompat.getColor(this, R.color.background)))
            )
            marker?.tag = place
        }
    }

    private fun showBottomDialog(place: Places) {
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.bottom_sheet_dialog)

        val placeNameTextView = dialog.findViewById<TextView>(R.id.placeNameTextView)
        val placeAddressTextView = dialog.findViewById<TextView>(R.id.placeAddressTextView)
        val placeImageView = dialog.findViewById<ImageView>(R.id.placeImageView)
        val routeButton = dialog.findViewById<Button>(R.id.routeButton)

        placeNameTextView?.text = place.name
        placeAddressTextView?.text = place.address
        placeImageView?.setImageResource(place.imageResId)

        routeButton?.setOnClickListener {
            val originLatLng = currentLocationMarker?.position
            val destinationLatLng = place.latLng

            if (originLatLng != null) {
                try {
                    getDirections(originLatLng, destinationLatLng)
                } catch (e: Exception) {
                    Log.e("MapsActivity2", "Error getting directions: ${e.message}")
                    showErrorToast("Erro ao traçar rota: ${e.message}")
                }
            } else {
                showErrorToast("Localização atual não disponível")
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun createGeoContext(): GeoApiContext {
        return GeoApiContext.Builder()
            .apiKey(getString(google_maps_key))
            .build()
    }

    private fun getDirections(origin: LatLng, destination: LatLng) {
        val geoApiContext = createGeoContext()

        DirectionsApi.newRequest(geoApiContext)
            .mode(TravelMode.DRIVING)
            .origin(origin.latitude.toString() + "," + origin.longitude)
            .destination(destination.latitude.toString() + "," + destination.longitude)
            .setCallback(object : PendingResult.Callback<DirectionsResult> {
                override fun onResult(result: DirectionsResult?) {
                    if (result != null && !result.routes.isNullOrEmpty()) {
                        val route = result.routes[0]
                        val leg = route.legs[0]
                        val steps = leg.steps

                        val polylineOptions = PolylineOptions()

                        for (step in steps) {
                            val polyline = step.polyline.decodePath()
                            val androidLatLngs = polyline.map { LatLng(it.lat, it.lng) }
                            polylineOptions.addAll(androidLatLngs)
                        }

                        runOnUiThread {
                            // Remove a polyline anterior, se existir
                            currentPolyline?.remove()

                            // Adiciona a nova polyline
                            currentPolyline = mMap?.addPolyline(polylineOptions)
                        }
                    } else {
                        showErrorToast("Nenhuma rota encontrada")
                    }
                }

                override fun onFailure(e: Throwable?) {
                    showErrorToast("Falha ao obter direções: ${e?.message}")
                }
            })
    }

    private fun showErrorToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@MapsActivity2, message, Toast.LENGTH_SHORT).show()
        }
    }
}

data class Places(
    val name: String,
    val latLng: LatLng,
    val address: String,
    val rating: Float,
    val imageResId: Int
)
