package com.example.weatherforecastapp.model.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Stations(
    val VABB: VABB,
    val VAJJ: VAJJ
) : Parcelable