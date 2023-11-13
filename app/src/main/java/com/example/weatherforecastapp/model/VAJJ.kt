package com.example.weatherforecastapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VAJJ(
    val contribution: Double,
    val distance: Double,
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val quality: Int,
    val useCount: Int
) : Parcelable