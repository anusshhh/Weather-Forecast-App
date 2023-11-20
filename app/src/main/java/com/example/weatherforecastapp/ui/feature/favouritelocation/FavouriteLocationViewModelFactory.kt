package com.example.weatherforecastapp.ui.feature.favouritelocation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecastapp.repository.IFavouriteLocationRepository

class FavouriteLocationViewModelFactory(private val repository: IFavouriteLocationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FavouriteLocationViewModel(repository) as T
    }
}
