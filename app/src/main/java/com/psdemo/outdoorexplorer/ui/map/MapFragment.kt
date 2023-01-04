package com.psdemo.outdoorexplorer.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.psdemo.outdoorexplorer.R
import com.psdemo.outdoorexplorer.data.OutdoorRoomDatabase
import com.psdemo.outdoorexplorer.data.OutdoorRoomRepository
import com.psdemo.outdoorexplorer.databinding.FragmentMapBinding

private const val REQUEST_LOCATION_PERMISSION = 1

class MapFragment : Fragment() {
    private lateinit var map: GoogleMap
    private var _binding: FragmentMapBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val outdoorDao = OutdoorRoomDatabase.getInstance(requireContext()).outdoorDao()
        val outdoorRepository = OutdoorRoomRepository(outdoorDao)
        val mapViewModelFactory = MapViewModelFactory(outdoorRepository)
        val mapViewModel = ViewModelProvider(this, mapViewModelFactory)[MapViewModel::class.java]

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map)
                as SupportMapFragment
        mapFragment.getMapAsync { map ->
            this.map = map
            // Center the map to the San Francisco area, since that's where
            // all of our location are located.
            val latitude = 37.68
            val longitude = -122.42
            val sanFranciscoLatLng = LatLng(latitude, longitude)
            // The zoom level controls how zoomed in you are on the map.
            val zoomLevel = 10f
            // Move the camera to homeLatLng
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(sanFranciscoLatLng, zoomLevel))
            map.uiSettings.isZoomControlsEnabled = true
            map.uiSettings.isTiltGesturesEnabled = false
            // This ensures that we're not adding markers to a map that isn't ready yet.
            mapViewModel.allLocations.observe(viewLifecycleOwner) { locations ->
                locations.forEach { location ->
                    val locationLatLng = LatLng(location.latitude, location.longitude)
                    val marker = map.addMarker(
                        MarkerOptions()
                            .position(locationLatLng)
                            .title(location.title)
                            .snippet("Hours: ${location.hours}")
                            .icon(
                                getBitmapFromVector(
                                    R.drawable.ic_star_black_24dp,
                                    R.color.colorAccent
                                )
                            )
                            .alpha(.75f)
                    )

                    marker?.tag = location.locationId
                    map.addCircle(
                        CircleOptions().center(locationLatLng).radius(location.geofenceRadius.toDouble())
                    )
                }

            }

            map.setOnInfoWindowClickListener { marker ->
                val action =
                    MapFragmentDirections.actionNavigationMapToNavigationLocation(marker.tag as Int)
                findNavController().navigate(action)
            }

            enableMyLocation()
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            } else {
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    showPermissionExplanationDialog()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
        } else {
            Snackbar.make(
                requireView(),
                getString(R.string.show_location_map),
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.ok) {
                    requestFineLocationPermission()
                }.show()
        }
    }

    private fun requestFineLocationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            showPermissionExplanationDialog()
        }else{
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }



    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.permission_denied_explanation)
            .setTitle(R.string.location_required_error)
            .setPositiveButton(
                "OK"
            ) { dialog, _ ->
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    REQUEST_LOCATION_PERMISSION
                )
                dialog.dismiss()
            }
            .setNegativeButton(
                "CANCEL"
            ) { dialog, _ ->
                enableMyLocation()
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getBitmapFromVector(
        @DrawableRes vectorResourceId: Int,
        @ColorRes colorResourceId: Int
    ): BitmapDescriptor {
        val vectorDrawable =
            ResourcesCompat.getDrawable(resources, vectorResourceId, requireContext().theme)
                ?: return BitmapDescriptorFactory.defaultMarker()

        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        DrawableCompat.setTint(
            vectorDrawable,
            ResourcesCompat.getColor(
                resources,
                colorResourceId, requireContext().theme
            )
        )
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun isPermissionGranted(): Boolean {
        // 1. Check if permissions are granted, if so, enable the my location layer
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    }
}
