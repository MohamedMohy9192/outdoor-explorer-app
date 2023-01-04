package com.psdemo.outdoorexplorer.ui.locations

import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.psdemo.outdoorexplorer.R
import com.psdemo.outdoorexplorer.data.MyLocation
import com.psdemo.outdoorexplorer.databinding.LocationItemBinding


class LocationsAdapter(private val onClickListener: OnClickListener) :
    RecyclerView.Adapter<LocationsAdapter.LocationHolder>() {
    private var allLocations: List<MyLocation> = ArrayList()
    private var currentLocation: Location? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationHolder {
        val itemView = LocationItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return LocationHolder(itemView)
    }

    override fun getItemCount(): Int {
        return allLocations.size
    }

    fun setLocations(locations: List<MyLocation>) {
        allLocations = locations
        notifyDataSetChanged()
    }

    fun setCurrentLocation(location: Location) {
        currentLocation = location
        allLocations = allLocations.sortedBy { it.getDistanceInMiles(location) }
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: LocationHolder, position: Int) {
        holder.bind(allLocations[position], onClickListener)
    }

    inner class LocationHolder(private val binding: LocationItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(location: MyLocation, clickListener: OnClickListener) {
            with(binding) {
                title.text = location.title
                card.setOnClickListener { clickListener.onClick(location.locationId) }

                if (currentLocation != null) {
                    distanceIcon.visibility = View.VISIBLE

                    distance.visibility = View.VISIBLE
                    distance.text = distance.context.getString(
                        R.string.distance_value,
                        location.getDistanceInMiles(currentLocation!!)
                    )
                }
            }
        }
    }

    interface OnClickListener {
        fun onClick(id: Int)
    }
}