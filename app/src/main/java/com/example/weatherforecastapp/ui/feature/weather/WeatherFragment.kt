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
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherforecastapp.R
import com.example.weatherforecastapp.dao.FavouriteLocationDao
import com.example.weatherforecastapp.database.FavouriteLocationDatabase
import com.example.weatherforecastapp.databinding.FragmentWeatherBinding
import com.example.weatherforecastapp.extensions.checkLocationPermission
import com.example.weatherforecastapp.extensions.getImageResource
import com.example.weatherforecastapp.extensions.gone
import com.example.weatherforecastapp.extensions.makeShortToast
import com.example.weatherforecastapp.extensions.visible
import com.example.weatherforecastapp.model.api.WeatherData
import com.example.weatherforecastapp.model.db.FavouriteLocation
import com.example.weatherforecastapp.network.ApiClient
import com.example.weatherforecastapp.network.ApiResponse
import com.example.weatherforecastapp.repository.FavouriteLocationRepositoryImpl
import com.example.weatherforecastapp.repository.WeatherRepositoryImpl
import com.example.weatherforecastapp.ui.adapter.WeatherForecastAdapter
import com.example.weatherforecastapp.utils.DateUtils
import com.example.weatherforecastapp.utils.FormattingUtils
import com.example.weatherforecastapp.utils.LocationUtils
import com.example.weatherforecastapp.viewmodel.FavouriteLocationViewModel
import com.example.weatherforecastapp.viewmodel.FavouriteLocationViewModelFactory
import com.example.weatherforecastapp.viewmodel.WeatherViewModel
import com.example.weatherforecastapp.viewmodel.WeatherViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class WeatherFragment : Fragment() {

    private var _binding: FragmentWeatherBinding? = null
    private val binding get() = _binding!!
    private lateinit var weatherViewModel: WeatherViewModel
    private var weatherForecastAdpter: WeatherForecastAdapter = WeatherForecastAdapter()
    private lateinit var favouriteLocationDao: FavouriteLocationDao
    private lateinit var favouriteLocationViewModel: FavouriteLocationViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var weatherForecastRecyclerView: RecyclerView
    private var favouriteLocation: FavouriteLocation? = null


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
        _binding = FragmentWeatherBinding.inflate(layoutInflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        weatherViewModel = ViewModelProvider(
            this,
            WeatherViewModelFactory(WeatherRepositoryImpl(ApiClient.weatherApiService))
        )[WeatherViewModel::class.java]

        favouriteLocationDao =
            FavouriteLocationDatabase.getDatabase(requireContext()).locationDao()
        favouriteLocationViewModel = ViewModelProvider(
            this,
            FavouriteLocationViewModelFactory(FavouriteLocationRepositoryImpl(favouriteLocationDao))
        )[FavouriteLocationViewModel::class.java]

        if (searchQuery != null) {
            updateWeather(searchQuery)
        } else if (latitude > -1 && longitude > -1) {
            Log.e("Check TAG", "Getting coordinates :$latitude , $longitude ")
            updateWeather(latitude, longitude)
        } else {
            if (requireContext().checkLocationPermission()) {
                binding.permissionDisabled.btnEnableLocation.text =
                    getString(R.string.turn_on_location)
                updatePermissionEnabledUI()
            } else {
                binding.permissionDisabled.btnEnableLocation.text =
                    getString(R.string.enable_location)
                updatePermissionDisabledUI()
            }
        }

        observeWeatherData()
        observeWeatherForecastData()
        observeAddFavouriteLocation()
        observeDeleteFavouriteLocation()

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
        if (binding.permissionEnabled.root.visibility != View.VISIBLE) {
            if (searchQuery != null) {
                updateWeather(searchQuery)
            } else if (latitude > -1 && longitude > -1) {
                updateWeather(latitude, longitude)
            } else {
                checkTheLocationStatusForUI()
            }
        }

    }

    private fun checkTheLocationStatusForUI() {
        if (requireContext().checkLocationPermission() && locationStatusCheck()) {
            showPermissionEnabledUI()
            fetchWeatherFromLocation()
        }
    }

    private fun showPermissionEnabledUI() {
        binding.permissionEnabled.root.visible()
        binding.permissionDisabled.root.gone()
    }

    private fun showPermissionDisabledUI() {
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
            fetchWeatherFromLocation()

        } else {
            buildAlertMessageNoGps()
        }
    }

    private fun fetchWeatherFromLocation() {
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
                weatherViewModel.getWeatherForecast(
                    location.latitude,
                    location.longitude
                )
                Log.e(
                    "TAG",
                    "fetchLocationForWeatherApi: ${location.latitude},${location.longitude}",
                )
            }
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

    fun observeWeatherData() {
        lifecycleScope.launch {
            weatherViewModel.weatherData.observe(viewLifecycleOwner) { response ->
                when (response) {
                    is ApiResponse.Success -> {
                        val weatherData = response.data
                        if (weatherData != null) {
                            updateUI(weatherData)
                        }
                    }

                    is ApiResponse.Error -> {
                        Log.e("TAG", "observeWeatherData: Error")
                    }

                    is ApiResponse.Loading -> {
                        Log.e("TAG", "observeWeatherData: Loading")
                    }
                }
            }
        }
    }

    fun observeWeatherForecastData() {
        lifecycleScope.launch {
            weatherViewModel.weatherForecastData.observe(viewLifecycleOwner) { response ->
                Log.e("TAG", "observeWeatherForecastData: $response")
                when (response) {
                    is ApiResponse.Success -> {
                        val weatherData = response.data
                        Log.e("TAG", "observeWeatherForecastData: ${weatherData?.days}")
                        if (weatherData != null) {
                            weatherForecastAdpter.submitList(response.data.days)
                        }
                    }

                    is ApiResponse.Error -> {
                        Log.e("TAG", "observeWeatherForecastData: Error")
                    }

                    is ApiResponse.Loading -> {
                        Log.e("TAG", "observeWeatherForecastData: Loading")
                    }
                }
            }
        }
    }

    @SuppressLint("DiscouragedApi") // Need to retrieve drawable based on icon name received from API.
    fun updateUI(weatherData: WeatherData) {

        val locationText =
            LocationUtils.getAddress(
                requireContext(),
                weatherData.latitude,
                weatherData.longitude
            )
        val uri = FormattingUtils.formatIconName(weatherData.currentConditions.icon)
        val imageResource = requireContext().getImageResource(uri)
        val date = DateUtils.convertDate(weatherData.days[0].datetime)
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val time = format.format(Date()).uppercase()

        binding.permissionEnabled.apply {
            locationAddress.text = locationText
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
                getString(
                    R.string.temperature,
                    weatherData.currentConditions.temp.toString()
                )
            weatherTitle.text = weatherData.currentConditions.conditions
            tvHumidityValue.text = weatherData.currentConditions.humidity.toString()
            tvWindValue.text = weatherData.currentConditions.windspeed.toString()
            tvPrecipitationValue.text = weatherData.currentConditions.precip.toString()

            weatherForecastRecyclerView = binding.permissionEnabled.rvWeatherForecast
            weatherForecastRecyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            weatherForecastRecyclerView.adapter = weatherForecastAdpter

            favouriteLocation = FavouriteLocation(
                name = locationText,
                latitude = weatherData.latitude,
                longitude = weatherData.longitude
            )

            //check if already in fav
            checkExistingFavoriteLocation()

            btnFavouriteLocation.setOnClickListener {
                toggleFavoriteLocation(favouriteLocation!!)
            }
        }
    }

    fun checkExistingFavoriteLocation() {
        lifecycleScope.launch {
            val existingLocation = favouriteLocationViewModel.getFavoriteLocationByCoordinates(
                favouriteLocation?.latitude ?: 0.0,
                favouriteLocation?.longitude ?: 0.0
            )

            if (existingLocation != null) {
                binding.permissionEnabled.btnFavouriteLocation.setImageResource(R.drawable.ic_favourite_filled)
            } else {
                binding.permissionEnabled.btnFavouriteLocation.setImageResource(R.drawable.ic_favourite_hollow)
            }
        }
    }

    fun toggleFavoriteLocation(favouriteLocation: FavouriteLocation) {
        lifecycleScope.launch {
            val existingLocation =
                favouriteLocationViewModel.getFavoriteLocationByCoordinates(
                    favouriteLocation.latitude,
                    favouriteLocation.longitude
                )
            if (existingLocation == null) {
                favouriteLocationViewModel.insertFavouriteLocation(favouriteLocation)
            } else {
                favouriteLocationViewModel.deleteFavouriteLocation(existingLocation)
            }
        }
    }


    private fun observeDeleteFavouriteLocation() {
        favouriteLocationViewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            if (result > 0) {
                Snackbar.make(
                    requireView(), getString(
                        R.string.deleteSuccess,
                        favouriteLocation?.name
                    ), Snackbar.LENGTH_SHORT
                ).show()
                binding.permissionEnabled.btnFavouriteLocation.setImageResource(R.drawable.ic_favourite_hollow)
            } else {
                Snackbar.make(
                    requireView(), getString(
                        R.string.deleteFail
                    ), Snackbar.LENGTH_SHORT
                ).show()
                binding.permissionEnabled.btnFavouriteLocation.setImageResource(R.drawable.ic_favourite_filled)
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

    /*private fun insertLocation(favouriteLocation: FavouriteLocation) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val insertResult = favouriteLocationViewModel.insertFavouriteLocation(favouriteLocation)
                   // favouriteLocationDao.addFavoriteLocation(favouriteLocation)
                withContext(Dispatchers.Main) {
                    if (insertResult > 0) {
                        Snackbar.make(
                            requireView(), getString(
                                R.string.insertSuccess,
                                favouriteLocation.name
                            ), Snackbar.LENGTH_LONG
                        ).show()
                        binding.permissionEnabled.btnFavouriteLocation.setImageResource(R.drawable.ic_favourite_filled)
                    } else {
                        Snackbar.make(
                            requireView(), getString(
                                R.string.insertFail
                            ), Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    requireContext().makeLongToast(getString(R.string.insertFail))
                }
            }
        }

    }*/

    private fun observeAddFavouriteLocation() {
        favouriteLocationViewModel.insertResult.observe(viewLifecycleOwner) { result ->
            if (result > 0) {
                Snackbar.make(
                    requireView(), getString(
                        R.string.insertSuccess,
                        favouriteLocation?.name
                    ), Snackbar.LENGTH_LONG
                ).show()
                binding.permissionEnabled.btnFavouriteLocation.setImageResource(R.drawable.ic_favourite_filled)
            } else {
                Snackbar.make(
                    requireView(), getString(
                        R.string.insertFail
                    ), Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }


    private fun updateWeather(searchQuery: String?) {
        if (searchQuery != null) {
            weatherViewModel.getCurrentWeatherData(searchQuery)
            weatherViewModel.getWeatherForecast(searchQuery)
        }
    }

    private fun updateWeather(latitude: Double, longitude: Double) {
        weatherViewModel.getCurrentWeatherData(latitude, longitude)
        weatherViewModel.getWeatherForecast(latitude, longitude)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {


            // Check if coming from FavouriteLocationFragment
            val isFromFavourites = arguments?.getBoolean("isFromFavourites") ?: false

            if (isFromFavourites) {
                // Clear the existing menu items
                menu.clear()
            } else {

                menuInflater.inflate(R.menu.menu, menu)


                val searchView = menu.findItem(R.id.search)?.actionView as SearchView
                searchView.queryHint = getString(R.string.searchHint)
                val favoriteMenuItem: MenuItem = menu.findItem(R.id.favourites)
                favoriteMenuItem.setOnMenuItemClickListener {
                    onFavoriteMenuItemClick()
                    true
                }

                searchView.setOnQueryTextListener(
                    object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            handleSearchQuery(query)
                            return false
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            return false
                        }
                    })
            }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return true
        }
    }

    private fun onFavoriteMenuItemClick() {
        //navController.popBackStack(R.id.favouriteLocationFragment, true)
        findNavController().navigate(R.id.action_weatherFragment_to_favouriteLocationFragment)
    }

    private fun handleSearchQuery(query: String?) {
        val inputQuery = query.toString()
        searchQuery = inputQuery

        if (inputQuery.isEmpty()) {
            requireContext().makeShortToast(getString(R.string.invalid_search_query))
        } else {
            weatherViewModel.onSearchQueryEntered(inputQuery)
        }
    }


    override fun onStart() {
        super.onStart()
        requireActivity().addMenuProvider(menuProvider)

    }

    override fun onStop() {
        super.onStop()
        requireActivity().removeMenuProvider(menuProvider)
    }
}
