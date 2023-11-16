package com.example.weatherforecastapp.utils

object FormattingUtils {

    /**
     *  This function is used to format the icon name received from the API to match it with the drawable file name.
     *  @param iconName The original icon name.
     *  @return The formatted icon name.
     */
    fun formatIconName(iconName: String): String {
        return iconName.replace("-", "_")
    }
}