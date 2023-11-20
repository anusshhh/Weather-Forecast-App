package com.example.weatherforecastapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecastapp.model.api.WeatherData
import com.example.weatherforecastapp.network.ApiResponse
import com.example.weatherforecastapp.repository.WeatherRepositoryImpl
import kotlinx.coroutines.launch

class WeatherViewModel(private val weatherRepositoryImpl: WeatherRepositoryImpl) : ViewModel() {
    private val _weatherData = MutableLiveData<ApiResponse<WeatherData?>>()
    val weatherData: LiveData<ApiResponse<WeatherData?>> get() = _weatherData

    private val _weatherForecastData = MutableLiveData<ApiResponse<WeatherData?>>()
    val weatherForecastData: LiveData<ApiResponse<WeatherData?>> get() = _weatherForecastData

    fun getCurrentWeatherData(query: String) {
        _weatherData.value =ApiResponse.Loading
        viewModelScope.launch {
            weatherRepositoryImpl.getCurrentWeatherData(query)
                .collect { weatherData ->
                    _weatherData.value = weatherData
                }
        }
    }

    fun getCurrentWeatherData(latitude: Double, longitude: Double) {
        _weatherData.value =ApiResponse.Loading
        viewModelScope.launch {
            weatherRepositoryImpl.getCurrentWeatherData(latitude, longitude)
                .collect { weatherData ->
                    _weatherData.value = weatherData
                }
        }
    }

    fun getWeatherForecast(query: String) {
        _weatherForecastData.value =ApiResponse.Loading
        viewModelScope.launch {
            weatherRepositoryImpl.getWeatherForecast(query)
                .collect { weatherForecastData ->
                    _weatherForecastData.value = weatherForecastData
                }
        }
    }

    fun getWeatherForecast(latitude: Double, longitude: Double) {
        _weatherForecastData.value=ApiResponse.Loading
        viewModelScope.launch {
            weatherRepositoryImpl.getWeatherForecast(latitude, longitude)
                .collect { weatherForecastData ->
                    _weatherForecastData.value = weatherForecastData
                }
        }
    }

    fun onSearchQueryEntered(query:String){
        getCurrentWeatherData(query)
        getWeatherForecast(query)
    }
}