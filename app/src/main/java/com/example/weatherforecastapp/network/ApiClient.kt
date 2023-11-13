package com.example.weatherforecastapp.network

import com.example.weatherforecastapp.api.WeatherApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL =
        "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/"
    private const val API_KEY = "G28TPVQC4L5Q28QKPD543JUK9"
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val weatherApiService: WeatherApiService by lazy {
        retrofit.create(WeatherApiService::class.java)
    }
}