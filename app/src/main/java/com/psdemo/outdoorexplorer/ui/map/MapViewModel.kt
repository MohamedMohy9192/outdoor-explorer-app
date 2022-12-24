package com.psdemo.outdoorexplorer.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.psdemo.outdoorexplorer.data.OutdoorRepository
import com.psdemo.outdoorexplorer.data.OutdoorRoomDatabase
import com.psdemo.outdoorexplorer.data.OutdoorRoomRepository

class MapViewModel(private val outdoorRepository: OutdoorRepository) : ViewModel() {

    val allLocations = outdoorRepository.getAllLocations()
}

class MapViewModelFactory(val outdoorRepository: OutdoorRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(outdoorRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}