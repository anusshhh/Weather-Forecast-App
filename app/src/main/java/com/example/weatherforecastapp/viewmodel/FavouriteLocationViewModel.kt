package com.example.weatherforecastapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecastapp.model.db.FavouriteLocation
import com.example.weatherforecastapp.repository.IFavouriteLocationRepository
import kotlinx.coroutines.launch

class FavouriteLocationViewModel(private val favouriteLocationRepository: IFavouriteLocationRepository) :
    ViewModel() {
    private val _favouriteLocations = MutableLiveData<List<FavouriteLocation>>()
    val favouriteLocations: MutableLiveData<List<FavouriteLocation>> get() = _favouriteLocations

    private val _insertResult = MutableLiveData<Long>()
    val insertResult: LiveData<Long>
        get() = _insertResult

    private val _deleteResult = MutableLiveData<Int>()
    val deleteResult: LiveData<Int>
        get() = _deleteResult


    init {
        getAllFavouriteLocations()
    }

    fun getAllFavouriteLocations() {
        viewModelScope.launch {
            favouriteLocationRepository.getAllFavoriteLocations()
                .collect { favouriteLocations ->
                    _favouriteLocations.value = favouriteLocations
                }
        }
    }

    suspend fun getFavoriteLocationByCoordinates(
        latitude: Double,
        longitude: Double
    ): FavouriteLocation? {

        return favouriteLocationRepository.getFavoriteLocationByCoordinates(
            latitude,
            longitude
        )
    }

    fun insertFavouriteLocation(favouriteLocation: FavouriteLocation) {
        viewModelScope.launch {
            if (getFavoriteLocationByCoordinates(favouriteLocation.latitude,favouriteLocation.longitude) == null
            ) {
                val result = favouriteLocationRepository.addFavoriteLocations(favouriteLocation)
                _insertResult.value = result
            } else {
                _insertResult.value = -1
            }
        }
    }

    fun deleteFavouriteLocation(favouriteLocation: FavouriteLocation) {
        viewModelScope.launch {
            val result = favouriteLocationRepository.deleteFavouriteLocation(favouriteLocation)
            _deleteResult.value = result
        }
    }


}