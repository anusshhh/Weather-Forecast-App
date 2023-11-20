package com.example.weatherforecastapp.extensions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

fun Context.getImageResource(uri: String): Int {
    return resources.getIdentifier(uri, "drawable", packageName)
}

fun Context.makeShortToast(message: String) {
    Toast.makeText(
        this, message, Toast.LENGTH_SHORT
    ).show()
}

fun Context.checkLocationPermission(): Boolean {
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

fun View.gone() {
    this.visibility = View.GONE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager =
        this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
    return networkCapabilities != null &&
            (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
}

fun Context.noInternetSnackbar(view: View, loadData: () -> Unit) : Snackbar{
    val snackbar = Snackbar.make(
        view,
        "No internet connection",
        Snackbar.LENGTH_INDEFINITE
    )
    snackbar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
        override fun onShown(transientBottomBar: Snackbar?) {
            super.onShown(transientBottomBar)
            transientBottomBar?.view?.findViewById<Button>(com.google.android.material.R.id.snackbar_action)
                ?.setOnClickListener {
                    if (this@noInternetSnackbar.isNetworkAvailable()) {
                        loadData.invoke()
                        snackbar.dismiss()
                    }
                }
        }
    })
    snackbar.setAction(
        "Retry"
    ) {
    }
    snackbar.show()
    return snackbar
}

