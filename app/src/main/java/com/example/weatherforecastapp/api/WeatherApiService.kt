package com.example.weatherforecastapp.api

import com.example.weatherforecastapp.model.WeatherData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

// TODO - need to move API KEY to gradle file
interface WeatherApiService {
    @GET("timeline/{query}/today?unitGroup=metric&include=current&key=G28TPVQC4L5Q28QKPD543JUK9&contentType=json")
    suspend fun getCurrentWeatherData(@Path("query") query: String): Response<WeatherData>

    @GET("timeline/{latitude},{longitude}/today?unitGroup=metric&include=current&key=G28TPVQC4L5Q28QKPD543JUK9&contentType=json")
    suspend fun getCurrentWeatherData(
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double
    ): Response<WeatherData>


}