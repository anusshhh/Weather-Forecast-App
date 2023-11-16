package com.example.weatherforecastapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecastapp.model.db.FavouriteLocation
import com.example.weatherforecastapp.repository.IFavouriteLocationRepository
import kotlinx.coroutines.launch

class FavouriteLocationViewModel(private val favouriteLocationRepository: IFavouriteLocationRepository) : ViewModel(){
    private val _favouriteLocations = MutableLiveData<List<FavouriteLocation>>()
    val favouriteLocations: MutableLiveData<List<FavouriteLocation>> get() = _favouriteLocations

    init {
        getAllFavouriteLocations()
    }

    fun getAllFavouriteLocations(){
        viewModelScope.launch {
            favouriteLocationRepository.getAllFavoriteLocations()
                .collect { favouriteLocations ->
                    _favouriteLocations.value = favouriteLocations
                }
        }
    }


}