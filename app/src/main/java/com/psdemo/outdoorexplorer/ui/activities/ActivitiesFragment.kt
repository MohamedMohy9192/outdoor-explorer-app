package com.psdemo.outdoorexplorer.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.psdemo.outdoorexplorer.BuildConfig
import com.psdemo.outdoorexplorer.GeofenceBroadcastReceiver
import com.psdemo.outdoorexplorer.R
import com.psdemo.outdoorexplorer.data.GeofencingChanges
import com.psdemo.outdoorexplorer.databinding.FragmentActivitiesBinding


private const val REQUEST_FINE_LOCATION_PERMISSION_REQUEST_CODE = 34
private const val REQUEST_BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 35
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1

class ActivitiesFragment : Fragment(), ActivitiesAdapter.OnClickListener {
    private lateinit var activitiesViewModel: ActivitiesViewModel
    private var _binding: FragmentActivitiesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var geofencingClient: GeofencingClient
    private var geofencingChanges: GeofencingChanges? = null
    private val pendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivitiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activitiesViewModel = ViewModelProvider(this)[ActivitiesViewModel::class.java]

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        val adapter = ActivitiesAdapter(this)
        binding.listActivities.adapter = adapter

        activitiesViewModel.allActivities.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            // Check if any of the geofences are enabled and both permissions are granted show a message to the user telling
            // them that we are using the background location
            if (it.any { activity -> activity.geofenceEnabled } && (checkFineLocationPermissionApproved() && checkBackgroundLocationPermissionApproved())) {
                Snackbar.make(
                    requireView(),
                    R.string.activities_background_reminder,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onClick(id: Int, title: String) {
        val action = ActivitiesFragmentDirections
            .actionNavigationActivitiesToNavigationLocations(id, "Locations with $title")
        val navController = Navigation.findNavController(requireView())
        navController.navigate(action)
    }

    override fun onGeofenceClick(id: Int) {
        geofencingChanges = activitiesViewModel.toggleGeofencing(id)
        handleGeofencing()
    }

    @SuppressLint("MissingPermission")
    private fun handleGeofencing() {
        when {
            !checkFineLocationPermissionApproved() -> requestFineLocationPermission()
            !checkBackgroundLocationPermissionApproved() -> requestBackgroundPermission()
            geofencingChanges != null -> {
                // First remove the old geofences and add the new ones
                if (geofencingChanges!!.idsToRemove.isNotEmpty()) {
                    geofencingClient.removeGeofences(geofencingChanges!!.idsToRemove)
                }

                if (geofencingChanges!!.locationsToAdd.isNotEmpty()) {
                    val geofencingRequest = GeofencingRequest.Builder().apply {
                        addGeofences(geofencingChanges!!.locationsToAdd)
                        setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER)
                    }.build()

                    geofencingClient.addGeofences(
                        geofencingRequest,
                        pendingIntent
                    )
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (
        // If the grantResults array is empty, then the interaction was interrupted and the permission request was cancelled.
            grantResults.isEmpty() ||
        // If the grantResults array's value at the LOCATION_PERMISSION_INDEX has a PERMISSION_DENIED,
        // it means that the user denied the fine location permission.
            (requestCode == REQUEST_FINE_LOCATION_PERMISSION_REQUEST_CODE &&
                    grantResults.first() == PackageManager.PERMISSION_DENIED) ||
            (requestCode == REQUEST_BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE &&
                    grantResults.first() == PackageManager.PERMISSION_DENIED)
        ) {
            // if permissions are not granted, so present a snack-bar explaining to the user
            // that the app needs location permissions
            Snackbar.make(
                binding.root,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else {
            handleGeofencing()
        }
    }

    private fun requestFineLocationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            Snackbar.make(
                requireView(),
                getString(R.string.locations_rationale),
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.ok) {
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_FINE_LOCATION_PERMISSION_REQUEST_CODE
                    )
                }.show()
        } else {
            Snackbar.make(
                requireView(),
                getString(R.string.location_permission_not_available),
                Snackbar.LENGTH_SHORT
            ).show()

            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FINE_LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun requestBackgroundPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            Snackbar.make(
                requireView(),
                getString(R.string.activities_background_rationale),
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.ok) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        requestPermissions(
                            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                            REQUEST_BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
                        )
                    }
                }.show()
        } else {
            Snackbar.make(
                requireView(),
                getString(R.string.background_location_permission_not_available),
                Snackbar.LENGTH_SHORT
            ).show()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    REQUEST_BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun checkFineLocationPermissionApproved(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkBackgroundLocationPermissionApproved(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
