package com.example.weatherforecastapp.ui.feature.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecastapp.repository.WeatherRepositoryImpl

class WeatherViewModelFactory(private val repository: WeatherRepositoryImpl) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WeatherViewModel(repository) as T
    }
}