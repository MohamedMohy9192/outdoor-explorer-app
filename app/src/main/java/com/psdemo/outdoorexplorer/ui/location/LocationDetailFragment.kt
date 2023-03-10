package com.psdemo.outdoorexplorer.ui.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.psdemo.outdoorexplorer.databinding.FragmentLocationDetailBinding

class LocationDetailFragment : Fragment() {
    private var _binding: FragmentLocationDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val locationDetailViewModel = ViewModelProvider(this)
            .get(LocationDetailViewModel::class.java)

        arguments?.let { bundle ->
            val passedArguments = LocationDetailFragmentArgs.fromBundle(bundle)
            locationDetailViewModel.getLocation(passedArguments.locationId)
                .observe(viewLifecycleOwner) { wrapper ->
                    with(binding) {
                        val location = wrapper.location
                        title.text = location.title
                        hours.text = location.hours
                        description.text = location.description
                        val adapter = ActivitiesAdapter()
                        listActivities.adapter = adapter
                        adapter.submitList(wrapper.activities.sortedBy { a -> a.title })
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
