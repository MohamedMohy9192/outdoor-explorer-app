package com.psdemo.outdoorexplorer.data

import androidx.lifecycle.LiveData

interface OutdoorRepository {
    fun getAllActivities(): LiveData<List<Activity>>

    fun getAllLocations(): LiveData<List<MyLocation>>

    fun getActivityWithLocations(activityId: Int): LiveData<ActivityWithLocations>

    fun getLocationById(locationId: Int): MyLocation

    fun getLocationWithActivities(locationId: Int): LiveData<LocationWithActivities>

    fun toggleActivityGeofence(id: Int): GeofencingChanges
}