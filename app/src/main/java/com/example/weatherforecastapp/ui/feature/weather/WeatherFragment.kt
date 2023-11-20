package com.example.weatherforecastapp.ui.feature.weather

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
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
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.weatherforecastapp.R
import com.example.weatherforecastapp.dao.FavouriteLocationDao
import com.example.weatherforecastapp.database.FavouriteLocationDatabase
import com.example.weatherforecastapp.databinding.FragmentWeatherBinding
import com.example.weatherforecastapp.extensions.checkLocationPermission
import com.example.weatherforecastapp.extensions.getImageResource
import com.example.weatherforecastapp.extensions.gone
import com.example.weatherforecastapp.extensions.invisible
import com.example.weatherforecastapp.extensions.isNetworkAvailable
import com.example.weatherforecastapp.extensions.makeShortToast
import com.example.weatherforecastapp.extensions.noInternetSnackbar
import com.example.weatherforecastapp.extensions.visible
import com.example.weatherforecastapp.model.api.WeatherData
import com.example.weatherforecastapp.model.db.FavouriteLocation
import com.example.weatherforecastapp.network.ApiClient
import com.example.weatherforecastapp.network.ApiResponse
import com.example.weatherforecastapp.repository.FavouriteLocationRepositoryImpl
import com.example.weatherforecastapp.repository.WeatherRepositoryImpl
import com.example.weatherforecastapp.ui.adapter.WeatherForecastAdapter
import com.example.weatherforecastapp.ui.feature.favouritelocation.FavouriteLocationViewModel
import com.example.weatherforecastapp.ui.feature.favouritelocation.FavouriteLocationViewModelFactory
import com.example.weatherforecastapp.utils.DateUtils
import com.example.weatherforecastapp.utils.FormattingUtils
import com.example.weatherforecastapp.utils.FormattingUtils.formatCoordinates
import com.example.weatherforecastapp.utils.LocationUtils
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
    private var weatherForecastAdapter: WeatherForecastAdapter = WeatherForecastAdapter()
    private lateinit var favouriteLocationDao: FavouriteLocationDao
    private lateinit var favouriteLocationViewModel: FavouriteLocationViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var weatherForecastRecyclerView: RecyclerView
    private var favouriteLocation: FavouriteLocation? = null
    private lateinit var progressBar: LottieAnimationView
    private var noInternetSnackbar: Snackbar? = null
    val snapHelper = LinearSnapHelper()


    var searchQuery: String? = null
    var latitude: Double = Double.NaN
    var longitude: Double = Double.NaN
    var isFromFavourites = false

    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
            }

            else -> {
                showPermissionExplanationDialog()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            searchQuery = it.getString("searchQuery")
            latitude = formatCoordinates(it.getDouble("latitude"))
            longitude = formatCoordinates(it.getDouble("longitude"))
            isFromFavourites = it.getBoolean("isFromFavourites")
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
        progressBar = binding.permissionEnabled.progressBar


        //internet check
        if (requireContext().isNetworkAvailable()) {
            loadData()
        } else {
            noInternetSnackbar = requireContext().noInternetSnackbar(requireView()) {
                loadData()
            }
        }

        // methods to observe
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
        binding.permissionEnabled.btnMyLocation.setOnClickListener {
            searchQuery = null
            progressBar.visible()
            binding.permissionEnabled.shimmerForecast.visible()
            hideWeatherDetailsViews()
            hideWeatherForecastViews()
            checkTheLocationStatusForUI()

        }
    }

    override fun onStart() {
        super.onStart()
        if (!isFromFavourites) {
            requireActivity().addMenuProvider(menuProvider)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!requireContext().isNetworkAvailable()) {
            noInternetSnackbar = requireContext().noInternetSnackbar(requireView()) {
                loadData()
            }
        }

        if (!requireContext().checkLocationPermission()) {
            binding.permissionDisabled.btnEnableLocation.text =
                getString(R.string.enable_location)
            updatePermissionDisabledUI()
        }

        if (binding.permissionEnabled.root.visibility != View.VISIBLE) {
            if (searchQuery != null) {
                updateWeather(searchQuery)
            } else if (!(latitude.isNaN() || longitude.isNaN())) {
                updateWeather(latitude, longitude)
            } else {
                checkTheLocationStatusForUI()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        noInternetSnackbar?.dismiss()
    }

    override fun onStop() {
        super.onStop()
        if (!isFromFavourites) {

            requireActivity().removeMenuProvider(menuProvider)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    // method for decision making
    private fun checkTheLocationStatusForUI() {
        if (requireContext().checkLocationPermission() && locationStatusCheck()) {
            updatePermissionEnabledUI()
        } else {
            updatePermissionDisabledUI()
        }
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

    //methods to load data
    fun loadData() {
        if (searchQuery != null) {
            updateWeather(searchQuery)
        } else if (!(latitude.isNaN() || longitude.isNaN())) {
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

    }

    private fun fetchWeatherFromLocation() {
        requireContext().checkLocationPermission()
        fusedLocationClient.getCurrentLocation(
            PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location: Location? ->
            location?.let {
                weatherViewModel.getCurrentWeatherData(
                    formatCoordinates(location.latitude),
                    formatCoordinates(location.longitude)
                )
                weatherViewModel.getWeatherForecast(
                    formatCoordinates(location.latitude),
                    formatCoordinates(location.longitude)
                )
            }
        }

    }

    @SuppressLint("DiscouragedApi") // Need to retrieve drawable based on icon name received from API.
    fun updateUI(weatherData: WeatherData) {
        val locationText =
            LocationUtils.getAddress(
                requireContext(),
                formatCoordinates(weatherData.latitude),
                formatCoordinates(weatherData.longitude)
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
            tvHumidityValue.text = getString(
                R.string.humidity_value,
                weatherData.currentConditions.humidity.toString()
            )
            tvPrecipitationValue.text = getString(
                R.string.precipitation_value,
                weatherData.currentConditions.precip.toString()
            )
            tvWindValue.text = getString(
                R.string.windspeed_value,
                weatherData.currentConditions.windspeed.toString()
            )

            weatherForecastRecyclerView = binding.permissionEnabled.rvWeatherForecast
            weatherForecastRecyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            snapHelper.attachToRecyclerView(weatherForecastRecyclerView)
            weatherForecastRecyclerView.adapter = weatherForecastAdapter

            favouriteLocation = FavouriteLocation(
                name = locationText,
                latitude = formatCoordinates(weatherData.latitude),
                longitude = formatCoordinates(weatherData.longitude)
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
                formatCoordinates(favouriteLocation?.latitude ?: 0.0),
                formatCoordinates(favouriteLocation?.longitude ?: 0.0)
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
                    formatCoordinates(favouriteLocation.latitude),
                    formatCoordinates(favouriteLocation.longitude)
                )
            if (existingLocation == null) {
                favouriteLocationViewModel.insertFavouriteLocation(favouriteLocation)
                Snackbar.make(
                    requireView(), getString(
                        R.string.insertSuccess,
                        favouriteLocation.name
                    ), Snackbar.LENGTH_LONG
                ).show()
            } else {
                favouriteLocationViewModel.deleteFavouriteLocation(existingLocation)
                Snackbar.make(
                    requireView(), getString(
                        R.string.deleteSuccess,
                        favouriteLocation?.name
                    ), Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    // observe methods
    fun observeWeatherData() {
        lifecycleScope.launch {
            weatherViewModel.weatherData.observe(viewLifecycleOwner) { response ->
                when (response) {
                    is ApiResponse.Success -> {
                        progressBar.gone()
                        val weatherData = response.data
                        if (weatherData != null) {
                            updateUI(weatherData)
                            showWeatherDetailsViews()
                        }
                    }

                    is ApiResponse.Error -> {
                        progressBar.invisible()
                        showWeatherDetailsErrorViews()
                        parameterCardLoadingView()
                        requireContext().makeShortToast(getString(R.string.no_data_found))
                    }

                    is ApiResponse.Loading -> {
                        progressBar.visible()
                        hideWeatherDetailsViews()
                        parameterCardLoadingView()
                        hideWeatherDetailsErrorViews()
                    }
                }
            }
        }
    }

    fun observeWeatherForecastData() {
        lifecycleScope.launch {
            weatherViewModel.weatherForecastData.observe(viewLifecycleOwner) { response ->
                when (response) {
                    is ApiResponse.Success -> {
                        val weatherData = response.data
                        if (weatherData != null) {
                            weatherForecastAdapter.submitList(response.data.days)
                            binding.permissionEnabled.shimmerForecast.gone()
                            showWeatherForecastViews()
                        }
                    }

                    is ApiResponse.Error -> {
                        hideWeatherForecastViews()
                        binding.permissionEnabled.shimmerForecast.gone()

                    }

                    is ApiResponse.Loading -> {
                        binding.permissionEnabled.shimmerForecast.visible()
                        binding.permissionEnabled.shimmerForecast.startShimmer()
                        hideWeatherForecastViews()
                    }
                }
            }
        }
    }

    private fun observeAddFavouriteLocation() {
        favouriteLocationViewModel.insertResult.observe(viewLifecycleOwner) { result ->
            if (result > 0) {
                binding.permissionEnabled.btnFavouriteLocation.setImageResource(R.drawable.ic_favourite_filled)
            } else {
                Snackbar.make(
                    requireView(), getString(
                        R.string.something_went_wrong
                    ), Snackbar.LENGTH_LONG
                ).show()
                binding.permissionEnabled.btnFavouriteLocation.setImageResource(R.drawable.ic_favourite_hollow)
            }
        }
    }

    private fun observeDeleteFavouriteLocation() {
        favouriteLocationViewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            if (result > 0) {
                binding.permissionEnabled.btnFavouriteLocation.setImageResource(R.drawable.ic_favourite_hollow)
            } else {
                Snackbar.make(
                    requireView(), getString(
                        R.string.something_went_wrong
                    ), Snackbar.LENGTH_SHORT
                ).show()
                binding.permissionEnabled.btnFavouriteLocation.setImageResource(R.drawable.ic_favourite_filled)
            }
        }
    }

    // GPS related methods
    fun locationStatusCheck(): Boolean {
        val manager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return (manager!!.isProviderEnabled(LocationManager.GPS_PROVIDER))
    }

    private fun buildAlertMessageNoGps() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setMessage(getString(R.string.no_gps_builder_message))
            .setCancelable(false)
            .setPositiveButton(
                "Yes"
            ) { dialog, id ->
                dialog.cancel()
                updatePermissionDisabledUI()
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

    // update weather data methods
    private fun updateWeather(searchQuery: String?) {
        if (searchQuery != null) {
            showPermissionEnabledUI()
            weatherViewModel.getCurrentWeatherData(searchQuery)
            weatherViewModel.getWeatherForecast(searchQuery)
        }
    }

    private fun updateWeather(latitude: Double, longitude: Double) {
        showPermissionEnabledUI()
        weatherViewModel.getCurrentWeatherData(latitude, longitude)
        weatherViewModel.getWeatherForecast(latitude, longitude)
    }


    // Toolbar Menu

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
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

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return true
        }
    }

    private fun onFavoriteMenuItemClick() {
        findNavController().navigate(R.id.action_weatherFragment_to_favouriteLocationFragment)
    }

    private fun handleSearchQuery(query: String?) {
        val inputQuery = query.toString()


        if (inputQuery.isEmpty()) {
            requireContext().makeShortToast(getString(R.string.invalid_search_query))
        } else {
            searchQuery = inputQuery
            loadData()
        }
    }

    // views manipulation
    private fun showPermissionEnabledUI() {
        binding.permissionEnabled.root.visible()
        binding.permissionDisabled.root.gone()
    }

    private fun showPermissionDisabledUI() {
        binding.permissionDisabled.root.visible()
        binding.permissionEnabled.root.gone()
    }

    private fun parameterCardLoadingView() {
        binding.permissionEnabled.tvHumidityValue.text = getString(R.string.empty_parameter)
        binding.permissionEnabled.tvWindValue.text = getString(R.string.empty_parameter)
        binding.permissionEnabled.tvPrecipitationValue.text = getString(R.string.empty_parameter)
    }

    private fun hideWeatherForecastViews() {
        binding.permissionEnabled.rvWeatherForecast.gone()
    }

    private fun showWeatherForecastViews() {
        binding.permissionEnabled.rvWeatherForecast.visible()
    }

    private fun hideWeatherDetailsViews() {
        binding.permissionEnabled.locationAddress.gone()
        binding.permissionEnabled.datetime.gone()
        binding.permissionEnabled.icon.gone()
        binding.permissionEnabled.temperature.gone()
        binding.permissionEnabled.weatherTitle.gone()
    }

    private fun hideWeatherDetailsErrorViews() {
        binding.permissionEnabled.ivNoDataFound.gone()
        binding.permissionEnabled.tvNoDataFound.gone()
        binding.permissionEnabled.tvSearchAgain.gone()
        binding.permissionEnabled.btnFavouriteLocation.visible()
    }

    private fun showWeatherDetailsErrorViews() {
        binding.permissionEnabled.ivNoDataFound.visible()
        binding.permissionEnabled.tvNoDataFound.visible()
        binding.permissionEnabled.tvSearchAgain.visible()
        binding.permissionEnabled.btnFavouriteLocation.gone()
    }

    private fun showWeatherDetailsViews() {
        binding.permissionEnabled.locationAddress.visible()
        binding.permissionEnabled.datetime.visible()
        binding.permissionEnabled.icon.visible()
        binding.permissionEnabled.temperature.visible()
        binding.permissionEnabled.weatherTitle.visible()
    }

    // Permission related methods
    private fun requestLocationPermission() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun showPermissionExplanationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.permission_alert_builder_title))
        builder.setMessage(getString(R.string.permission_alert_builder_message))
        builder.setPositiveButton(getString(R.string.permission_alert_builder_positive)) { _, _ ->
            openAppSettings()
        }
        builder.setNegativeButton(getString(R.string.permission_alert_builder_negative)) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent)
    }


}
