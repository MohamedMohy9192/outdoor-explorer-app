package com.psdemo.outdoorexplorer.ui.locations

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.material.snackbar.Snackbar
import com.psdemo.outdoorexplorer.BuildConfig
import com.psdemo.outdoorexplorer.R
import com.psdemo.outdoorexplorer.databinding.FragmentLocationsBinding

private const val TAG = "LocationsFragment"
private const val REQUEST_FINE_LOCATION_PERMISSION_REQUEST_CODE = 34
private const val LOCATION_PERMISSION_INDEX = 0
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29

class LocationsFragment : Fragment(), LocationsAdapter.OnClickListener {

    private var _binding: FragmentLocationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var adapter: LocationsAdapter
    private lateinit var locationsViewModel: LocationsViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        locationsViewModel = ViewModelProvider(this)
            .get(LocationsViewModel::class.java)

        adapter = LocationsAdapter(this)
        binding.listLocations.adapter = adapter

        arguments?.let { bundle ->
            val passedArguments = LocationsFragmentArgs.fromBundle(bundle)
            if (passedArguments.activityId == 0) {
                locationsViewModel.allLocations.observe(viewLifecycleOwner, Observer {
                    adapter.setLocations(it)
                    it.forEach { myLocation ->
                        Log.d(TAG, "onViewCreated: $myLocation")
                    }
                })
            } else {
                locationsViewModel.locationsWithActivity(passedArguments.activityId)
                    .observe(viewLifecycleOwner, Observer {
                        adapter.setLocations(it.locations)
                    })
            }
        }


    }

    override fun onStart() {
        super.onStart()
        checkPermissionsAndRequestCurrentLocation()
    }

    override fun onClick(id: Int) {
        val action = LocationsFragmentDirections
            .actionNavigationLocationsToNavigationLocation(id)

        val navController = Navigation.findNavController(requireView())
        navController.navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkPermissionsAndRequestCurrentLocation() {
        if (checkFineLocationPermissionApproved()) {
            Snackbar.make(
                requireView(),
                getString(R.string.location_permission_available),
                Snackbar.LENGTH_SHORT
            ).show()
            checkDeviceLocationSettingsAndGetCurrentLocation()
        } else {
            Snackbar.make(
                requireView(),
                getString(R.string.show_locations_distance),
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.ok) {
                    requestFineLocationPermission()
                }.show()
        }
    }

    private fun checkFineLocationPermissionApproved(): Boolean {
        val fineLocationApproved = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocationApproved
    }

    private fun checkDeviceLocationSettingsAndGetCurrentLocation(resolve: Boolean = true) {
        val locationRequest = LocationRequest.Builder(1000)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        //  check the location settings.
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        //  finding out if the location settings are not satisfied
        locationSettingsResponseTask.addOnFailureListener { exception ->
            Log.d(TAG, "addOnFailureListener: ")
            // Check if the exception is of type ResolvableApiException,
            // and if so, prompt the user to turn on device location.
            if (exception is ResolvableApiException && resolve) {
                try {
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        REQUEST_TURN_DEVICE_LOCATION_ON,
                        null,
                        0,
                        0,
                        0,
                        null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                // If the exception is not of type ResolvableApiException, present a snackbar
                Snackbar.make(
                    binding.root,
                    R.string.location_required_error,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndGetCurrentLocation()
                }.show()
            }
        }

        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d(TAG, "checkDeviceLocationSettingsAndGetCurrentLocation: ")
                requestCurrentLocation()
            }
        }

    }

    @SuppressLint("MissingPermission")
    private fun requestCurrentLocation() {
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            Log.d(TAG, "requestCurrentLocation: $location")
             if (location != null) {
            adapter.setCurrentLocation(location)
              }
        }
    }

    private fun requestFineLocationPermission() {
        // If the permissions have already been granted,
        // you don't need to ask again, so you can return out of the method.
        if (checkFineLocationPermissionApproved())
            return

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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if ( // If the grantResults array is empty, then the interaction was interrupted and the permission request was cancelled.
            grantResults.isEmpty() ||
            // If the grantResults array's value at the LOCATION_PERMISSION_INDEX has a PERMISSION_DENIED,
            // it means that the user denied the fine location permission.
            (requestCode == REQUEST_FINE_LOCATION_PERMISSION_REQUEST_CODE &&
                    grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED)
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
            Snackbar.make(
                requireView(),
                getString(R.string.location_permission_granted),
                Snackbar.LENGTH_SHORT
            ).show()
            // Otherwise, permissions have been granted
            checkDeviceLocationSettingsAndGetCurrentLocation()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            Log.d(TAG, "onActivityResult: ")
            checkDeviceLocationSettingsAndGetCurrentLocation(false)

        }
    }
}
