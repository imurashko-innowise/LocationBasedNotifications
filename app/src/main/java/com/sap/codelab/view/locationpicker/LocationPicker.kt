package com.sap.codelab.view.locationpicker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityLocationPickerBinding

internal class LocationPicker : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityLocationPickerBinding
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var selectedLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationPickerBinding.inflate(layoutInflater)
        binding.confirmLocation.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        setContentView(binding.root)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val cancellationTokenSource = CancellationTokenSource()

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                location?.let {
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(it.latitude, it.longitude),
                            DEFAULT_CAMERA_ZOOM,
                        )
                    )
                }
            }
        }

        // Listen for taps on the map
        map.setOnMapClickListener { latLng ->
            selectedLatLng = latLng
            updateIntentResult()
            map.clear() // Remove old markers
            map.addMarker(MarkerOptions().position(latLng).title(MARKER_TITLE))
        }
    }

    private fun updateIntentResult() {
        selectedLatLng?.let {
            val resultIntent = Intent().apply {
                putExtra(RESULT_LOCATION_NAME, it)
            }
            setResult(RESULT_OK, resultIntent)
        }
    }

    companion object {
        const val RESULT_LOCATION_NAME = "latLong"
        private const val MARKER_TITLE = "Selected location"
        private const val DEFAULT_CAMERA_ZOOM = 15f
    }
}
