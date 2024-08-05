package com.duyvv.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.duyvv.map.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private var fusedLocationClient: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getLocationByGoogleServices()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true

        requestLocationPermission()
    }

    private fun requestLocationPermission() {
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getLocationByGoogleServices()
        }
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocationByGoogleServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient?.lastLocation?.addOnCompleteListener { task ->
                task.result?.let {
                    mMap.isMyLocationEnabled = true
                    Log.d("TAG", "onLocationResult: $it")
                    val currentLocation = LatLng(it.latitude, it.longitude)
                    val targetLocation = LatLng(10.762622, 106.660172)
                    addMarker(currentLocation, targetLocation)
                    calculateDistance(currentLocation, targetLocation)
                }
            }
        }
    }

    private fun addMarker(currentLocation: LatLng, targetLocation: LatLng) {
        mMap.addMarker(
            MarkerOptions().position(currentLocation)
                .title("Current location")
        )
        mMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(currentLocation, 15f)
        )
        mMap.addMarker(
            MarkerOptions().position(targetLocation)
                .title("Selected location")
        )
        mMap.addPolyline(
            PolylineOptions()
                .clickable(true)
                .add(currentLocation, targetLocation)
                .width(10f)
                .color(Color.RED)
        )
        val builder = LatLngBounds.Builder()
        builder.include(currentLocation)
        builder.include(targetLocation)
        val bounds = builder.build()
        val padding = 300
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        mMap.animateCamera(cameraUpdate)
    }

    private fun calculateDistance(currentLocation: LatLng, targetLocation: LatLng) {
        val results = FloatArray(1)
        Location.distanceBetween(
            currentLocation.latitude,
            currentLocation.longitude,
            targetLocation.latitude,
            targetLocation.longitude,
            results
        )
        val distanceInMeters = results[0]
        mMap.setOnPolylineClickListener {
            val distanceInKm = distanceInMeters / 1000
            val distanceText = "Khoảng cách: %.2f km".format(distanceInKm)
            Toast.makeText(this, distanceText, Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}