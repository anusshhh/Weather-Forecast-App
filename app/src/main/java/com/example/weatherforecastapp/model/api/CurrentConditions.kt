package com.example.weatherforecastapp.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue


@Parcelize
data class CurrentConditions(
    val cloudcover: Double,
    val conditions: String,
    val datetime: String,
    val datetimeEpoch: Int,
    val dew: Double,
    val feelslike: Double,
    val humidity: Double?,
    val icon: String,
    val moonphase: Double,
    val precip: Double,
    val precipprob: Double,
    val preciptype: Double,
    val pressure: Double,
    val snow: Double,
    val snowdepth: Double,
    val solarenergy: Double,
    val solarradiation: Double,
    val source: String,
    val stations: List<String>,
    val sunrise: String,
    val sunriseEpoch: Int,
    val sunset: String,
    val sunsetEpoch: Int,
    val temp: Double,
    val uvindex: Double,
    val visibility: Double,
    val winddir: Double,
    val windgust: Double,
    val windspeed: Double
) : Parcelable