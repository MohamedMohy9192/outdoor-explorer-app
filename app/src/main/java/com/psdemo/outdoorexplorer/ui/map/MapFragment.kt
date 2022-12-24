package com.psdemo.outdoorexplorer.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.psdemo.outdoorexplorer.R
import com.psdemo.outdoorexplorer.data.OutdoorRoomDatabase
import com.psdemo.outdoorexplorer.data.OutdoorRoomRepository
import com.psdemo.outdoorexplorer.databinding.FragmentMapBinding

class MapFragment : Fragment() {
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
                }
            }

            map.setOnInfoWindowClickListener { marker ->
                val action =
                    MapFragmentDirections.actionNavigationMapToNavigationLocation(marker.tag as Int)
                findNavController().navigate(action)
            }
        }


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
}
