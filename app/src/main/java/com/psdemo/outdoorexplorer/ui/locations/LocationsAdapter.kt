package com.psdemo.outdoorexplorer.ui.locations

import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.psdemo.outdoorexplorer.R
import com.psdemo.outdoorexplorer.data.MyLocation
import com.psdemo.outdoorexplorer.databinding.LocationItemBinding


class LocationsAdapter(private val onClickListener: OnClickListener) :
    ListAdapter<MyLocation, LocationsAdapter.LocationHolder>(DiffCallback) {

    private var currentLocation: Location? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationHolder {
        val itemView = LocationItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return LocationHolder(itemView)
    }

    fun setCurrentLocation(location: Location) {
        currentLocation = location
        submitList(currentList.sortedBy { it.getDistanceInMiles(location) })
    }

    override fun onBindViewHolder(holder: LocationHolder, position: Int) {
        holder.bind(getItem(position), onClickListener)
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

    companion object DiffCallback : DiffUtil.ItemCallback<MyLocation>() {
        override fun areItemsTheSame(oldItem: MyLocation, newItem: MyLocation): Boolean {
            return oldItem.locationId == newItem.locationId
        }

        override fun areContentsTheSame(oldItem: MyLocation, newItem: MyLocation): Boolean {
            return oldItem == newItem
        }

    }
}