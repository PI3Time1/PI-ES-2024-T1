package br.com.sentinellock

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.GeoPoint

class MapsActivity2 : AppCompatActivity(), OnMapReadyCallback {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mMap: GoogleMap? = null
    private var currentLocationMarker: Marker? = null
    private var currentPolyline: Polyline? = null

    private var selectedItemId: Int = R.id.action_map

    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_map -> {
                    // Handle map action
                    return@OnNavigationItemSelectedListener true
                }
                R.id.action_look -> {
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

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps2)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        savedInstanceState?.getInt("selectedItemId")?.let {
            selectedItemId = it
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        bottomNavigationView.selectedItemId = selectedItemId

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_look -> {
                    startActivity(Intent(this, TelaArmarioActivity::class.java))
                    true
                }
                R.id.action_map -> {
                    startActivity(Intent(this, MapsActivity2::class.java))
                    true
                }
                R.id.action_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        db = FirebaseFirestore.getInstance()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("selectedItemId", selectedItemId) // Salvar o ID do item selecionado
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
        db.collection("unidade_de_locacao")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val place = document.toObject(Places::class.java)
                    val marker = mMap?.addMarker(
                        MarkerOptions()
                            .title(place.name)
                            .snippet(place.address)
                            .position(place.latLng.toLatLng()) // Convert GeoPoint to LatLng
                            .icon(BitmapHelper.vectorToBitmap(this, R.drawable.maps_logo_vetor, ContextCompat.getColor(this, R.color.background)))
                    )
                    marker?.tag = place
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

    private fun showBottomDialog(place: Places) {
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.bottom_sheet_dialog)

        val placeNameTextView = dialog.findViewById<TextView>(R.id.placeNameTextView)
        val placeAddressTextView = dialog.findViewById<TextView>(R.id.placeAddressTextView)
        val routeButton = dialog.findViewById<Button>(R.id.routeButton)
        val alugaButton = dialog.findViewById<Button>(R.id.alugaButton)

        placeNameTextView?.text = place.name
        placeAddressTextView?.text = place.address

        routeButton?.setOnClickListener {
            val originLatLng = currentLocationMarker?.position
            val destinationLatLng = place.latLng.toLatLng()

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

        alugaButton?.setOnClickListener {
            val originLatLng = currentLocationMarker?.position
            val destinationLatLng = place.latLng.toLatLng()

            if (originLatLng != null) {
                try {
                    val distance = calculateDistance(originLatLng, destinationLatLng)
                    if (distance <= 1000) { // 1000 meters = 1 km
                        // Criar uma Intent para iniciar a próxima Activity
                        val intent = Intent(this@MapsActivity2, AlugarArmarioActivity::class.java)
                        // Adicionar os valores do marcador como extras
                        intent.putExtra("nome", place.name)
                        intent.putExtra("precoMeiaHora", place.prcMeiaHora)
                        intent.putExtra("precoUmaHora", place.prcUmaHora)
                        intent.putExtra("precoDuasHoras", place.prcUmaHora)
                        intent.putExtra("precoQuatroHoras", place.prcQuatroHora)
                        intent.putExtra("promocao", place.promocao)
                        // Iniciar a próxima Activity
                        startActivity(intent)
                    } else {
                        showDialogTooFar()
                    }
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

    private fun showDialogTooFar() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_out_of_range)

        val window = dialog.window

        window?.setGravity(Gravity.CENTER)

        val closeButton = dialog.findViewById<Button>(R.id.closeButton)
        closeButton?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun calculateDistance(origin: LatLng, destination: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            origin.latitude, origin.longitude,
            destination.latitude, destination.longitude,
            results
        )
        return results[0]
    }

    private fun createGeoContext(): GeoApiContext {
        return GeoApiContext.Builder()
            .apiKey(getString(R.string.google_maps_key))
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

    companion object {
        private const val TAG = "MapsActivity2"
    }
}

data class Places(
    val name: String = "",
    val latLng: GeoPoint = GeoPoint(0.0, 0.0), // Keep the type as GeoPoint
    val address: String = "",
    val rating: Float = 0.0f,
    val prcMeiaHora: Int = 0,
    val prcUmaHora: Int = 0,
    val prcuasHora: Int = 0,
    val prcQuatroHora: Int = 0,
    val promocao: Int = 0
)

fun GeoPoint.toLatLng(): LatLng {
    return LatLng(this.latitude, this.longitude)
}