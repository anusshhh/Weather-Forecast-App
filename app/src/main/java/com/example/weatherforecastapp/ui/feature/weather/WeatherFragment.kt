package com.example.weatherforecastapp.ui.feature.weather

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.weatherforecastapp.R
import com.example.weatherforecastapp.dao.FavouriteLocationDao
import com.example.weatherforecastapp.database.FavouriteLocationDatabase
import com.example.weatherforecastapp.databinding.FragmentWeatherBinding
import com.example.weatherforecastapp.extensions.checkLocationPermission
import com.example.weatherforecastapp.extensions.getImageResource
import com.example.weatherforecastapp.extensions.gone
import com.example.weatherforecastapp.extensions.makeLongToast
import com.example.weatherforecastapp.extensions.visible
import com.example.weatherforecastapp.model.api.WeatherData
import com.example.weatherforecastapp.model.db.FavouriteLocation
import com.example.weatherforecastapp.network.ApiClient
import com.example.weatherforecastapp.network.ApiResponse
import com.example.weatherforecastapp.repository.WeatherRepositoryImpl
import com.example.weatherforecastapp.utils.DateUtils
import com.example.weatherforecastapp.utils.FormattingUtils
import com.example.weatherforecastapp.utils.LocationUtils
import com.example.weatherforecastapp.viewmodel.WeatherViewModel
import com.example.weatherforecastapp.viewmodel.WeatherViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class WeatherFragment : Fragment() {

    private lateinit var binding: FragmentWeatherBinding
    private lateinit var weatherViewModel: WeatherViewModel
    private lateinit var favouriteLocationDao: FavouriteLocationDao
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    var searchQuery: String? = null
    var latitude: Double = -1.0
    var longitude: Double = -1.0

    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
            }

            else -> {
                // No location access granted.
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            searchQuery = it.getString("searchQuery")
            latitude = it.getDouble("latitude")
            longitude = it.getDouble("longitude")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeatherBinding.inflate(layoutInflater)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        weatherViewModel = ViewModelProvider(
            this,
            WeatherViewModelFactory(WeatherRepositoryImpl(ApiClient.weatherApiService))
        )[WeatherViewModel::class.java]

        favouriteLocationDao = FavouriteLocationDatabase.getDatabase(requireContext()).locationDao()


        if (searchQuery != null) {
            updateWeather(searchQuery)
        } else {
            if (requireContext().checkLocationPermission()) {
                binding.permissionDisabled.btnEnableLocation.text =
                    getString(R.string.turn_on_location)
               // updatePermissionEnabledUI()
            } else {
                binding.permissionDisabled.btnEnableLocation.text =
                    getString(R.string.enable_location)
                updatePermissionDisabledUI()
            }
        }

        observeSearchWeatherData()

        //OnClickListeners
        binding.permissionDisabled.btnEnableLocation.setOnClickListener {
            if (requireContext().checkLocationPermission()) {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            } else {
                requestLocationPermission()
            }
        }



    }

    override fun onResume() {
        super.onResume()

        //UI update
        //Location fetch
        checkTheLocationStatusForUI()
    }

    private fun checkTheLocationStatusForUI() {
        if (requireContext().checkLocationPermission() && locationStatusCheck()) {
            showPermissionEnabledUI()
            fetchLocationForWeatherApi()
        }
    }

    private fun showPermissionEnabledUI() {
        Log.e("TAG", "showPermissionDisabledUI: showing permission enable")
        binding.permissionEnabled.root.visible()
        binding.permissionDisabled.root.gone()
    }

    private fun showPermissionDisabledUI() {
        Log.e("TAG", "showPermissionDisabledUI: showing permission disable")
        binding.permissionDisabled.root.visible()
        binding.permissionEnabled.root.gone()
    }


    private fun updatePermissionDisabledUI() {
        showPermissionDisabledUI()
        if (requireContext().checkLocationPermission()) {
            binding.permissionDisabled.btnEnableLocation.text = getString(R.string.turn_on_location)
            binding.permissionDisabled.btnEnableLocation.setOnClickListener {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        } else {
            binding.permissionDisabled.btnEnableLocation.text =
                getString(R.string.enable_location)
            binding.permissionDisabled.btnEnableLocation.setOnClickListener {
                requestLocationPermission()
            }
        }

    }

    private fun updatePermissionEnabledUI() {
        showPermissionEnabledUI()

        requireContext().checkLocationPermission()
        if (locationStatusCheck()) {
            fetchLocationForWeatherApi()

        } else {
            buildAlertMessageNoGps()
        }
    }

    private fun fetchLocationForWeatherApi() {
        requireContext().checkLocationPermission()
        fusedLocationClient.getCurrentLocation(
            PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location: Location? ->
            location?.let {
                weatherViewModel.getCurrentWeatherData(
                    location.latitude,
                    location.longitude
                )
                Log.e(
                    "TAG",
                    "fetchLocationForWeatherApi: ${location.latitude},${location.longitude}",
                )
            }
        }
        if (latitude > -1 && longitude > -1) {
            updateWeather(latitude, longitude)
        }
    }

    private fun requestLocationPermission() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    fun observeSearchWeatherData() {
        lifecycleScope.launch {
            weatherViewModel.weatherData.observe(viewLifecycleOwner) { response ->
                when (response) {
                    is ApiResponse.Success -> {
                        Log.e("TAG", "observeSearchWeatherData: Success")
                        val weatherData = response.data
                        Log.d("Updated TAG", "onCreate: ${response.data}")
                        if (weatherData != null) {
                            updateUI(weatherData)
                        }
                    }

                    is ApiResponse.Error -> {
                        Log.e("TAG", "observeSearchWeatherData: Error")
                    }

                    is ApiResponse.Loading -> {
                        Log.e("TAG", "observeSearchWeatherData: Loading")
                    }
                }
            }
        }
    }


    @SuppressLint("DiscouragedApi") // Need to retrieve drawable based on icon name received from API.
    fun updateUI(weatherData: WeatherData) {

        val location =
            LocationUtils.getAddress(requireContext(), weatherData.latitude, weatherData.longitude)
        val uri = FormattingUtils.formatIconName(weatherData.currentConditions.icon)
        val imageResource = requireContext().getImageResource(uri)
        val date = DateUtils.convertDate(weatherData.days[0].datetime)
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val time = format.format(Date()).uppercase()

        binding.permissionEnabled.apply {
            locationAddress.text = location
            datetime.text = getString(R.string.dateTimeString, date, time)

            if (imageResource != 0) {
                val res = ContextCompat.getDrawable(requireContext(), imageResource)
                icon.setImageDrawable(res)
            } else {
                icon.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_unknown_weather
                    )
                )
            }

            temperature.text =
                getString(R.string.temperature, weatherData.currentConditions.temp.toString())
            weatherTitle.text = weatherData.currentConditions.conditions
            tvHumidityValue.text = weatherData.currentConditions.humidity.toString()
            tvWindValue.text = weatherData.currentConditions.windspeed.toString()
            tvPrecipitationValue.text = weatherData.currentConditions.precip.toString()

            btnFavouriteLocation.setOnClickListener {
                val favouriteLocation = FavouriteLocation(
                    name = location,
                    latitude = weatherData.latitude,
                    longitude = weatherData.longitude
                )
                Log.d("TAG", "Location in update ui: $favouriteLocation")
                insertLocation(favouriteLocation)

            }
        }
    }

    fun locationStatusCheck(): Boolean {
        val manager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return (manager!!.isProviderEnabled(LocationManager.GPS_PROVIDER))
    }

    private fun buildAlertMessageNoGps() {
        Log.e("TAG", "buildAlertMessageNoGps: Alert builder created")
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton(
                "Yes"
            ) { dialog, id ->
                dialog.cancel()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton(
                "No"
            ) { dialog, id ->
                dialog.cancel()
                updatePermissionDisabledUI()
            }

        val alert: AlertDialog = builder.create()
        alert.show()
    }

    private fun insertLocation(favouriteLocation: FavouriteLocation) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val insertResult = favouriteLocationDao.insertFavoriteLocation(favouriteLocation)
                Log.d("TAG", "insertLocation: $insertResult ")
                withContext(Dispatchers.Main) {
                    if (insertResult > 0) {
                        requireContext().makeLongToast(
                            getString(
                                R.string.insertSuccess,
                                favouriteLocation.name
                            )
                        )
                    } else {
                        requireContext().makeLongToast(getString(R.string.insertFail))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    requireContext().makeLongToast(getString(R.string.insertFail))
                }
            }
        }
    }


    private fun updateWeather(searchQuery: String?) {
        if (searchQuery != null) {
            Log.e("TAG", "updateUI: $searchQuery")
            weatherViewModel.getCurrentWeatherData(searchQuery)
        }
    }

    private fun updateWeather(latitude: Double, longitude: Double) {
        weatherViewModel.getCurrentWeatherData(latitude, longitude)
    }

}
