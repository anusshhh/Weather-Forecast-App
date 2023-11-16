package com.example.weatherforecastapp.utils

import android.content.Context
import android.location.Geocoder
import android.util.Log
import java.io.IOException
import java.util.Locale

object LocationUtils {
    fun getAddress(context: Context, latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        lateinit var addressText : String

        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            addressText = if (addresses?.isNotEmpty() == true) {
                val address = addresses[0]
                val city = address.locality ?: ""
                val state = address.adminArea ?: ""
                val country = address.countryName ?: ""
                "$city, $state, $country"
            } else {
                "Could not find location."
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return addressText
    }
}