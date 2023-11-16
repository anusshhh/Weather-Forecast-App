package com.example.weatherforecastapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecastapp.model.api.WeatherData
import com.example.weatherforecastapp.network.ApiResponse
import com.example.weatherforecastapp.repository.WeatherRepositoryImpl
import kotlinx.coroutines.launch

class WeatherViewModel (private val weatherRepositoryImpl: WeatherRepositoryImpl) : ViewModel() {
    private val _weatherData = MutableLiveData<ApiResponse<WeatherData?>>()
    val weatherData: LiveData<ApiResponse<WeatherData?>> get() = _weatherData

    fun getCurrentWeatherData(query: String) {
        viewModelScope.launch {
            weatherRepositoryImpl.getCurrentWeatherData(query)
                .collect { weatherData ->
                    _weatherData.value = weatherData
                }
        }
    }

    fun getCurrentWeatherData(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            weatherRepositoryImpl.getCurrentWeatherData(latitude, longitude)
                .collect { weatherData ->
                    _weatherData.value = weatherData
                }
        }

    }
}