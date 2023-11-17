package com.example.weatherforecastapp.repository

import com.example.weatherforecastapp.model.api.WeatherData
import com.example.weatherforecastapp.network.ApiResponse
import kotlinx.coroutines.flow.Flow

interface IWeatherRepository {
    fun getCurrentWeatherData(query: String): Flow<ApiResponse<WeatherData?>>

    fun getCurrentWeatherData(latitude: Double, longitude: Double): Flow<ApiResponse<WeatherData?>>

    fun getWeatherForecast(query: String): Flow<ApiResponse<WeatherData?>>

    fun getWeatherForecast(latitude: Double, longitude: Double): Flow<ApiResponse<WeatherData?>>
}