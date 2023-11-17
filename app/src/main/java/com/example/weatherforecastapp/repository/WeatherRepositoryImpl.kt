package com.example.weatherforecastapp.repository

import com.example.weatherforecastapp.model.api.WeatherData
import com.example.weatherforecastapp.network.ApiResponse
import com.example.weatherforecastapp.network.WeatherApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class WeatherRepositoryImpl constructor(private val weatherApiService: WeatherApiService) :
    IWeatherRepository {
    override fun getCurrentWeatherData(query: String): Flow<ApiResponse<WeatherData?>> = flow {
        val result = weatherApiService.getCurrentWeatherData(query)
        if (result.isSuccessful) {
            emit(ApiResponse.Success(result.body()))
        } else {
            emit(ApiResponse.Error("Failed to fetch data"))
        }
    }.flowOn(Dispatchers.IO).catch {
        emit(ApiResponse.Error(it.message.toString()))
    }

    override fun getCurrentWeatherData(
        latitude: Double,
        longitude: Double
    ): Flow<ApiResponse<WeatherData?>> = flow {

        val result = weatherApiService.getCurrentWeatherData(latitude, longitude)
        if (result.isSuccessful) {
            emit(ApiResponse.Success(result.body()))
        } else {
            emit(ApiResponse.Error("Failed to fetch data"))
        }
    }.flowOn(Dispatchers.IO).catch {
        emit(ApiResponse.Error(it.message.toString()))
    }

    override fun getWeatherForecast(query: String): Flow<ApiResponse<WeatherData?>> = flow {
        val result = weatherApiService.getWeatherForecast(query)
        if (result.isSuccessful) {
            emit(ApiResponse.Success(result.body()))
        } else {
            emit(ApiResponse.Error("Failed to fetch data"))
        }
    }.flowOn(Dispatchers.IO).catch {
        emit(ApiResponse.Error(it.message.toString()))
    }

    override fun getWeatherForecast(
        latitude: Double,
        longitude: Double
    ): Flow<ApiResponse<WeatherData?>> = flow {
        val result = weatherApiService.getWeatherForecast(latitude, longitude)
        if (result.isSuccessful) {
            emit(ApiResponse.Success(result.body()))
        } else {
            emit(ApiResponse.Error("Failed to fetch data"))
        }
    }.flowOn(Dispatchers.IO).catch {
        emit(ApiResponse.Error(it.message.toString()))
    }


}