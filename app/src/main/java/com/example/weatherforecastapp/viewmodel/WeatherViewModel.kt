package com.example.weatherforecastapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecastapp.model.WeatherData
import com.example.weatherforecastapp.network.ApiResponse
import com.example.weatherforecastapp.repository.WeatherRepository
import kotlinx.coroutines.launch

class WeatherViewModel (private val weatherRepository: WeatherRepository) : ViewModel() {
    private val _weatherData = MutableLiveData<ApiResponse<WeatherData?>>()
    val weatherData: LiveData<ApiResponse<WeatherData?>> get() = _weatherData

    fun getCurrentWeatherData(query: String) {
        viewModelScope.launch {
            weatherRepository.getCurrentWeatherData(query)
                .collect { weatherData ->
                    _weatherData.value = weatherData
                }
        }
    }

    fun getCurrentWeatherData(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            weatherRepository.getCurrentWeatherData(latitude, longitude)
                .collect { weatherData ->
                    _weatherData.value = weatherData
                }
        }

    }
}