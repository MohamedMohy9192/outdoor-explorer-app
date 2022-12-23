package com.psdemo.outdoorexplorer.ui.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.psdemo.outdoorexplorer.databinding.FragmentActivitiesBinding

class ActivitiesFragment : Fragment(), ActivitiesAdapter.OnClickListener {
    private lateinit var activitiesViewModel: ActivitiesViewModel
    private var _binding: FragmentActivitiesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivitiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activitiesViewModel = ViewModelProvider(this)
            .get(ActivitiesViewModel::class.java)

        val adapter = ActivitiesAdapter(this)
        binding.listActivities.adapter = adapter

        activitiesViewModel.allActivities.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }

    override fun onClick(id: Int, title: String) {
        val action = ActivitiesFragmentDirections
            .actionNavigationActivitiesToNavigationLocations(id, "Locations with $title")
        val navController = Navigation.findNavController(requireView())
        navController.navigate(action)
    }

    override fun onGeofenceClick(id: Int) {
        TODO("Not yet implemented")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
