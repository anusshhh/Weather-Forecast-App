package com.example.weatherforecastapp.model.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VABB(
    val contribution: Double,
    val distance: Double,
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val quality: Int,
    val useCount: Int
) : Parcelable