package com.duyvv.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
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

    private var currentPoint: LatLng? = null

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

    private fun requestLocationPermission() {
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getLocationByGoogleServices()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true

        requestLocationPermission()

        mMap.setOnMapClickListener { latLng ->
            mMap.addMarker(
                MarkerOptions().position(latLng)
                    .title("Selected location")
            )
            if (currentPoint != null) {
                mMap.addPolyline(
                    PolylineOptions()
                        .add(currentPoint, latLng)
                        .width(5f)
                        .color(Color.RED)
                )
                val builder = LatLngBounds.Builder()
                builder.include(currentPoint!!)
                builder.include(latLng)
                val bounds = builder.build()
                val padding = 300
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                mMap.animateCamera(cameraUpdate)
            }
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
                    currentPoint = LatLng(it.latitude, it.longitude)
                    mMap.addMarker(
                        MarkerOptions().position(currentPoint!!)
                            .title("Current location")
                    )
                    mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(currentPoint!!, 15f)
                    )
                }
            }
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}