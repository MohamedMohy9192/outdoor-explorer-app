package com.psdemo.outdoorexplorer.ui.locations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.psdemo.outdoorexplorer.databinding.FragmentLocationsBinding

class LocationsFragment : Fragment(), LocationsAdapter.OnClickListener {

    private var _binding: FragmentLocationsBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var adapter: LocationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val locationsViewModel = ViewModelProvider(this)
            .get(LocationsViewModel::class.java)

        adapter = LocationsAdapter(this)
        binding.listLocations.adapter = adapter

        arguments?.let { bundle ->
            val passedArguments = LocationsFragmentArgs.fromBundle(bundle)
            if (passedArguments.activityId == 0) {
                locationsViewModel.allLocations.observe(viewLifecycleOwner, Observer {
                    adapter.submitList(it)
                })
            } else {
                locationsViewModel.locationsWithActivity(passedArguments.activityId)
                    .observe(viewLifecycleOwner, Observer {
                        adapter.submitList(it.locations)
                    })
            }
        }
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
}
