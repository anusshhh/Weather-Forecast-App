package com.example.weatherforecastapp.utils

import android.content.Context
import android.location.Geocoder
import java.io.IOException
import java.util.Locale

object LocationUtils {
    fun getAddress(context: Context, latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        var addressText: String = ""

        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            addressText = if (addresses?.isNotEmpty() == true) {
                val address = addresses[0]
                val city = address.locality.orEmpty()
                val state = address.adminArea.orEmpty()
                val country = address.countryName.orEmpty()

                // Conditionally append components
                addressText = buildString {
                    if (city.isNotEmpty()) {
                        append(city)
                        append(", ")
                    }
                    if (state.isNotEmpty()) {
                        append(state)
                        append(", ")
                    }
                    if (country.isNotEmpty()) {
                        append(country)
                    }
                }

                if (addressText.isEmpty()) {
                    "Could not find location."
                } else {
                    addressText
                }
            } else {
                "Could not find location."
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return addressText
    }
}
