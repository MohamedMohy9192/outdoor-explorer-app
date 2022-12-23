package com.psdemo.outdoorexplorer.ui.activities

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.psdemo.outdoorexplorer.R
import com.psdemo.outdoorexplorer.data.Activity
import com.psdemo.outdoorexplorer.databinding.ActivityItemBinding

class ActivitiesAdapter(private val onClickListener: OnClickListener) :
    ListAdapter<Activity, ActivitiesAdapter.ActivityHolder>(DiffCallback) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityHolder {
        val itemView =
            ActivityItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActivityHolder(itemView)
    }

    override fun onBindViewHolder(holder: ActivityHolder, position: Int) {
        holder.bind(getItem(position), onClickListener)
    }

    inner class ActivityHolder(private val binding: ActivityItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(activity: Activity, clickListener: OnClickListener) {
            with(binding) {
                title.text = activity.title

                card.setOnClickListener {
                    clickListener.onClick(activity.activityId, activity.title)
                }
                geofence.setOnClickListener { clickListener.onGeofenceClick(activity.activityId) }

                var color = R.color.colorGray
                if (activity.geofenceEnabled) {
                    color = R.color.colorAccent
                }

                ImageViewCompat
                    .setImageTintList(
                        geofence,
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                geofence.context,
                                color
                            )
                        )
                    )

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

    interface OnClickListener {
        fun onClick(id: Int, title: String)
        fun onGeofenceClick(id: Int)
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