package com.example.weatherforecastapp.repository

import com.example.weatherforecastapp.model.WeatherData
import com.example.weatherforecastapp.network.ApiResponse
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    fun getCurrentWeatherData(query: String):  Flow<ApiResponse<WeatherData?>>

    fun getCurrentWeatherData(latitude: Double, longitude: Double):  Flow<ApiResponse<WeatherData?>>
}