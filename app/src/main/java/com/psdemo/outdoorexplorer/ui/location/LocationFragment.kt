package com.psdemo.outdoorexplorer.ui.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.psdemo.outdoorexplorer.databinding.FragmentLocationBinding

class LocationFragment : Fragment() {
    private var _binding: FragmentLocationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val locationViewModel = ViewModelProvider(this)
            .get(LocationViewModel::class.java)

        arguments?.let { bundle ->
            val passedArguments = LocationFragmentArgs.fromBundle(bundle)
            locationViewModel.getLocation(passedArguments.locationId)
                .observe(viewLifecycleOwner, Observer { wrapper ->
                    with(binding) {
                        val location = wrapper.location
                        title.text = location.title
                        hours.text = location.hours
                        description.text = location.description
                        val adapter = ActivitiesAdapter()
                        listActivities.adapter = adapter
                        adapter.submitList(wrapper.activities.sortedBy { a -> a.title })
                    }
                })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
