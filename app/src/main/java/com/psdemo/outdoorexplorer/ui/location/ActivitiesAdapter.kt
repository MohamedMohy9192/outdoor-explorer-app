package com.psdemo.outdoorexplorer.ui.location

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.psdemo.outdoorexplorer.data.Activity
import com.psdemo.outdoorexplorer.databinding.LocationActivityItemBinding


class ActivitiesAdapter :
    ListAdapter<Activity, ActivitiesAdapter.ActivityHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityHolder {
        val itemView = LocationActivityItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ActivityHolder(itemView)
    }

    override fun onBindViewHolder(holder: ActivityHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ActivityHolder(private val binding: LocationActivityItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(activity: Activity) {
            with(binding) {
                title.text = activity.title

                val iconUri = "drawable/ic_${activity.icon}_black_24dp"
                val imageResource: Int =
                    icon.context.resources.getIdentifier(
                        iconUri, null, icon.context.packageName
                    )
                icon.setImageResource(imageResource)
                icon.contentDescription = activity.title
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Activity>() {
        override fun areItemsTheSame(oldItem: Activity, newItem: Activity): Boolean {
            return oldItem.activityId == newItem.activityId
        }

        override fun areContentsTheSame(oldItem: Activity, newItem: Activity): Boolean {
            return oldItem == newItem
        }

    }
}