package com.example.weatherforecastapp.utils

object FormattingUtils {
    fun formatIconName(iconName: String): String {
        return iconName.replace("-", "_")
    }
    fun formatCoordinates(coordinate:Double) : Double{
        return String.format("%.2f", coordinate).toDouble()
    }

}