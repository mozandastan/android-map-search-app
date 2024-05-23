package com.ozzystudio.mapsearchapp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.ozzystudio.mapsearchapp.databinding.ActivityMapsBinding
import java.io.IOException
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location
    private lateinit var placesClient: PlacesClient // for search results

    private lateinit var autocompleteFragment: AutocompleteSupportFragment //autocomplete search bar
    private var marker: Marker? = null // global marker
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val apiKey = getApiKey() // get API KEY from manifest

        Places.initialize(applicationContext, apiKey) //for auto search results
        placesClient = Places.createClient(this)

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this) //for current location

        autocompleteFragment = supportFragmentManager.findFragmentById(R.id.place_autocomplete) as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
            )
        )
        buildAutoCompleteSearch() //activate autocomplete with Listener

        checkLocationPermission() // Check location permission
    }

    private fun getApiKey(): String {
        val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        return appInfo.metaData.getString("com.google.android.geo.API_KEY") ?: "default_key"
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    private fun setMarker(
        latLng: LatLng,
        address: String,
        zoomF: Float
    ) { // camera and marker operations on the map
        marker?.remove() //remove the old marker
        val markerOptions = MarkerOptions().position(latLng).title(address)
        marker = mMap.addMarker(markerOptions) // Mark the selected location on the map
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                latLng,
                zoomF
            )
        ) // Focus camera on selected location
    }

    private fun buildAutoCompleteSearch() {
        // autocomplete search bar with places
        autocompleteFragment.setHint("Where is your location")

        autocompleteFragment.setOnPlaceSelectedListener(object :
            PlaceSelectionListener { // add listener
            override fun onPlaceSelected(place: Place) { // when a result selected
                val address = place.address.toString()
                binding.txtAddress.text = address //view address in the text

                val latLng = place.latLng
                setMarker(latLng, address, 17f)
            }

            override fun onError(status: Status) {
                Toast.makeText(applicationContext, status.toString(), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkLocationPermission() {
        //location permission function
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // If the user has given location permission, get the location.
            getDeviceLocation() // get the current location
        } else {
            //Show location permission request
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                Companion.LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Companion.LOCATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // User granted location permission, get location.
                    getDeviceLocation()
                } else {
                    // User did not grant location permission. Send message
                    Toast.makeText(
                        this,
                        "Access to the location was not allowed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1 //permission state val
    }

    private fun getDeviceLocation() {
        // find and go to the current location
        try {
            val locationResult = fusedLocationClient.lastLocation // try to reach the location
            locationResult.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) { // if results found
                    currentLocation = task.result

                    val address = getDeviceLocationAddress(currentLocation) //getting open address for a text
                    binding.txtAddress.text = address

                    val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
                    setMarker(latLng, address, 18f)

                } else {
                    // Error case, go to the default location
                    Log.e("MapsActivity", "Current location is null. Using defaults.")
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(0.0, 0.0), 15f))
                }
            }
        } catch (e: SecurityException) {
            //no location permission
            Log.e("Exception: %s", e.message.toString())
        }
    }

    private fun getDeviceLocationAddress(currentLoc: Location): String {
        // function for location to open address string
        val geocoder = Geocoder(this@MapsActivity, Locale.getDefault())
        var addressText = ""
        try {
            val addresses: List<Address>? =
                geocoder.getFromLocation(currentLoc.latitude, currentLoc.longitude, 1)

            if (addresses != null && addresses.isNotEmpty()) {
                val address: Address = addresses[0]
                val stringBuilder = StringBuilder()

                // Merge address lines
                for (i in 0..address.maxAddressLineIndex) {
                    stringBuilder.append(address.getAddressLine(i)).append("\n")
                }
                addressText = stringBuilder.toString()
            } else {
                addressText = "No address found"
            }
        } catch (e: IOException) {
            e.printStackTrace()
            addressText = "Unable to get address for the location"
        }

        return addressText // return all string address
    }
}