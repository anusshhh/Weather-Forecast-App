package com.example.weatherforecastapp.extensions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.weatherforecastapp.R

fun Context.getImageResource(uri: String): Int {
    return resources.getIdentifier(uri, "drawable", packageName)
}

fun Context.makeShortToast(message: String){
    Toast.makeText(this, message, Toast.LENGTH_SHORT
    ).show()
}
fun Context.makeLongToast(message: String){
    Toast.makeText(this, message, Toast.LENGTH_LONG
    ).show()
}

fun Context.checkLocationPermission() : Boolean {
    val fineLocationPermission =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    val coarseLocationPermission =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    return fineLocationPermission || coarseLocationPermission

}

fun View.gone(){
    this.visibility= View.GONE
}

fun View.visible(){
    this.visibility= View.VISIBLE
}

