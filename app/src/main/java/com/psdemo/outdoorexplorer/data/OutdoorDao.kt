package com.psdemo.outdoorexplorer.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface OutdoorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLocations(locations: List<MyLocation>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertActivities(activities: List<Activity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertActivityLocationCrossRefs(activityLocationCrossRefs: List<ActivityLocationCrossRef>)

    @Query("SELECT * FROM Activity ORDER BY title")
    fun getAllActivities(): LiveData<List<Activity>>

    @Query("SELECT * FROM MyLocation")
    fun getAllLocations(): LiveData<List<MyLocation>>

    @Transaction
    @Query("SELECT * FROM Activity WHERE activityId = :activityId")
    fun getActivityWithLocations(activityId: Int): LiveData<ActivityWithLocations>

    @Query("SELECT * FROM MyLocation WHERE locationId = :locationId")
    fun getLocationById(locationId: Int): MyLocation

    @Transaction
    @Query("SELECT DISTINCT L.* FROM MyLocation L, Activity A, ActivityLocationCrossRef AL WHERE L.locationId = AL.locationId AND AL.activityId = A.activityId AND A.geofenceEnabled != 0")
    fun getLocationsForGeofencing(): List<MyLocation>

    @Transaction
    @Query("SELECT * FROM MyLocation WHERE locationId = :locationId")
    fun getLocationWithActivities(locationId: Int): LiveData<LocationWithActivities>

    @Query("UPDATE activity set geofenceEnabled = ~geofenceEnabled WHERE activityId = :id")
    fun toggleGeofenceEnabled(id: Int): Int
}