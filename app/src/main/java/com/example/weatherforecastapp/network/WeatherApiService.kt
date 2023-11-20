package com.example.weatherforecastapp.network

import com.example.weatherforecastapp.BuildConfig
import com.example.weatherforecastapp.model.api.WeatherData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

// TODO - need to move API KEY to gradle file
interface WeatherApiService {
    @GET("timeline/{query}/today?unitGroup=metric&include=current&key=${BuildConfig.API_KEY}&contentType=json")
    suspend fun getCurrentWeatherData(@Path("query") query: String): Response<WeatherData>

    @GET("timeline/{latitude},{longitude}/today?unitGroup=metric&include=current&key=${BuildConfig.API_KEY}&contentType=json")
    suspend fun getCurrentWeatherData(
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double
    ): Response<WeatherData>

    @GET("timeline/{query}?unitGroup=metric&include=days%2Ccurrent&key=${BuildConfig.API_KEY}&contentType=json")
    suspend fun getWeatherForecast(@Path("query") query: String): Response<WeatherData>

    @GET("timeline/{latitude},{longitude}?unitGroup=metric&include=days%2Ccurrent&key=${BuildConfig.API_KEY}&contentType=json")
    suspend fun getWeatherForecast(
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double
    ): Response<WeatherData>


}