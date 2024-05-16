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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint

// Atividade principal para mostrar um mapa com funcionalidades relacionadas ao local
class MapsActivity2 : AppCompatActivity(), OnMapReadyCallback {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mMap: GoogleMap? = null
    private var currentLocationMarker: Marker? = null
    private var currentPolyline: Polyline? = null

    private var selectedItemId: Int = R.id.action_map

    // Listener para mudanças de itens na barra de navegação inferior
    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_map -> {
                    // Ação para o mapa
                    return@OnNavigationItemSelectedListener true
                }
                R.id.action_look -> {
                    // Ação para pesquisa
                    return@OnNavigationItemSelectedListener true
                }
                R.id.action_profile -> {
                    // Ação para perfil
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps2)

        // Inicializa o cliente de localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Inicializa o Firebase Firestore
        db = FirebaseFirestore.getInstance()

        // Recupera o mapa da interface do usuário de mapa (MapFragment)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Restaura o item selecionado na barra de navegação inferior
        savedInstanceState?.getInt("selectedItemId")?.let {
            selectedItemId = it
        }

        // Configura a barra de navegação inferior
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        // Define o item selecionado na barra de navegação inferior
        bottomNavigationView.selectedItemId = selectedItemId

        // Configura o listener para os itens da barra de navegação inferior
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_look -> {
                    // Abre a atividade de visualização
                    startActivity(Intent(this, TelaArmarioActivity::class.java))
                    true
                }
                R.id.action_map -> {
                    // Permanece nesta atividade (mapa)
                    startActivity(Intent(this, MapsActivity2::class.java))
                    true
                }
                R.id.action_profile -> {
                    // Abre a atividade do perfil
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Salva o ID do item selecionado
        outState.putInt("selectedItemId", selectedItemId)
    }

    // Callback chamado quando o mapa está pronto para ser usado
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        enableMyLocation() // Habilita a localização do usuário no mapa
        addMarkers() // Adiciona marcadores (pins) ao mapa

        // Configura o listener para clique em marcadores
        mMap?.setOnMarkerClickListener { marker ->
            val place = marker.tag as? Places
            place?.let {
                showBottomDialog(it) // Mostra um dialog com detalhes do local ao clicar no marcador
            }
            true
        }
    }

    // Habilita a localização do usuário no mapa
    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permite a exibição da localização do usuário no mapa
            mMap?.isMyLocationEnabled = true
            moveCameraToCurrentLocation() // Move a câmera para a localização atual do usuário
        } else {
            // Solicita permissão para acessar a localização do dispositivo
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    // Move a câmera para a localização atual do usuário
    private fun moveCameraToCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Obtém a última localização conhecida do dispositivo
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    // Move a câmera para a localização atual com zoom
                    mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    addOrUpdateCurrentLocationMarker(currentLatLng) // Adiciona ou atualiza o marcador da localização atual
                }
            }
        }
    }

    // Adiciona ou atualiza o marcador da localização atual
    private fun addOrUpdateCurrentLocationMarker(latLng: LatLng) {
        if (currentLocationMarker == null) {
            // Adiciona um novo marcador na localização atual
            currentLocationMarker = mMap?.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
        } else {
            // Atualiza a posição do marcador da localização atual
            currentLocationMarker?.position = latLng
        }
    }

    // Adiciona marcadores (pins) ao mapa a partir dos dados do Firebase Firestore
    private fun addMarkers() {
        db.collection("unidade_de_locacao")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val id = document.id // Obtém o id do documento
                    val place = document.toObject(Places::class.java)
                    // Define o id do lugar
                    place.id = id
                    // Adiciona um marcador no mapa com os detalhes do local
                    val marker = mMap?.addMarker(
                        MarkerOptions()
                            .title(place.name)
                            .snippet(place.address)
                            .position(place.latLng.toLatLng())
                            .icon(BitmapHelper.vectorToBitmap(this, R.drawable.maps_logo_vetor, ContextCompat.getColor(this, R.color.background)))
                    )
                    marker?.tag = place // Associa o objeto Places ao marcador
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

    // Mostra um dialog com detalhes do local ao clicar no marcador
    private fun showBottomDialog(place: Places) {
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.bottom_sheet_dialog)

        val placeNameTextView = dialog.findViewById<TextView>(R.id.placeNameTextView)
        val placeAddressTextView = dialog.findViewById<TextView>(R.id.placeAddressTextView)
        val routeButton = dialog.findViewById<Button>(R.id.routeButton)
        val alugaButton = dialog.findViewById<Button>(R.id.alugaButton)

        placeNameTextView?.text = place.name
        placeAddressTextView?.text = place.address

        // Configura o botão para traçar rota até o local
        routeButton?.setOnClickListener {
            val originLatLng = currentLocationMarker?.position
            val destinationLatLng = place.latLng.toLatLng()

            if (originLatLng != null) {
                try {
                    getDirections(originLatLng, destinationLatLng) // Obtém e exibe as direções para o local
                } catch (e: Exception) {
                    Log.e("MapsActivity2", "Error getting directions: ${e.message}")
                    showErrorToast("Erro ao traçar rota: ${e.message}")
                }
            } else {
                showErrorToast("Localização atual não disponível")
            }

            dialog.dismiss() // Fecha o dialog
        }

        // Configura o botão para alugar um armário no local
        alugaButton?.setOnClickListener {
            if (isUserLoggedIn()) {
                val originLatLng = currentLocationMarker?.position
                val destinationLatLng = place.latLng.toLatLng()

                if (originLatLng != null) {
                    try {
                        val distance = calculateDistance(originLatLng, destinationLatLng)
                        if (distance <= 1000) { // Verifica se o local está a menos de 1 km de distância
                            // Cria uma Intent para iniciar a atividade de aluguel de armário
                            val intent = Intent(this@MapsActivity2, AlugarArmarioActivity::class.java)
                            // Adiciona os detalhes do local como extras na Intent
                            intent.putExtra("id", place.id) // Passa o ID para a próxima tela
                            intent.putExtra("nome", place.name)
                            intent.putExtra("precoMeiaHora", place.prcMeiaHora)
                            intent.putExtra("precoUmaHora", place.prcUmaHora)
                            intent.putExtra("precoDuasHoras", place.prcUmaHora)
                            intent.putExtra("precoQuatroHoras", place.prcQuatroHora)
                            intent.putExtra("promocao", place.promocao)
                            // Inicia a atividade de aluguel de armário
                            startActivity(intent)
                        } else {
                            showDialogTooFar() // Mostra um dialog informando que o local está muito longe
                        }
                    } catch (e: Exception) {
                        Log.e("MapsActivity2", "Error getting directions: ${e.message}")
                        showErrorToast("Erro ao traçar rota: ${e.message}")
                    }
                } else {
                    showErrorToast("Localização atual não disponível")
                }
            } else {
                // Usuário não está logado, redireciona para LoginActivity
                startActivity(Intent(this@MapsActivity2, LoginActivity::class.java))
            }

            dialog.dismiss() // Fecha o dialog
        }

        dialog.show() // Mostra o dialog na tela
    }

    private fun isUserLoggedIn(): Boolean {
        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        return currentUser != null // Retorna true se o usuário estiver logado
    }
    // Mostra um dialog informando que o local está muito longe
    private fun showDialogTooFar() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_out_of_range)

        val window = dialog.window

        window?.setGravity(Gravity.CENTER)

        val closeButton = dialog.findViewById<Button>(R.id.closeButton)
        closeButton?.setOnClickListener {
            dialog.dismiss() // Fecha o dialog
        }

        dialog.show() // Mostra o dialog na tela
    }

    // Calcula a distância em metros entre dois pontos no mapa
    private fun calculateDistance(origin: LatLng, destination: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            origin.latitude, origin.longitude,
            destination.latitude, destination.longitude,
            results
        )
        return results[0] // Retorna a distância em metros
    }

    // Cria e retorna um contexto para a API de Geo do Google
    private fun createGeoContext(): GeoApiContext {
        return GeoApiContext.Builder()
            .apiKey(getString(R.string.google_maps_key))
            .build()
    }

    // Obtém e exibe as direções (rota) entre dois pontos no mapa
    private fun getDirections(origin: LatLng, destination: LatLng) {
        val geoApiContext = createGeoContext()

        DirectionsApi.newRequest(geoApiContext)
            .mode(TravelMode.DRIVING)
            .origin(origin.latitude.toString() + "," + origin.longitude)
            .destination(destination.latitude.toString() + "," + destination.longitude)
            .setCallback(object : PendingResult.Callback
            <DirectionsResult> {
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
    var id: String = "", // Adiciona o campo id
    val name: String = "",
    val latLng: GeoPoint = GeoPoint(0.0, 0.0),
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