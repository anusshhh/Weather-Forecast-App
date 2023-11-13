package com.example.weatherforecastapp.network

sealed class ApiResponse<out T> {

    data class Success<out T>(val data: T) : ApiResponse<T>()

    data class Error(val message: String? = null) : ApiResponse<Nothing>()

    object Loading : ApiResponse<Nothing>()
}