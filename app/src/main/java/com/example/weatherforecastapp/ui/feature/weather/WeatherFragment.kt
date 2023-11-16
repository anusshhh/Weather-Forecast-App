package com.example.weatherforecastapp.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.weatherforecastapp.R
import com.example.weatherforecastapp.dao.FavouriteLocationDao
import com.example.weatherforecastapp.database.FavouriteLocationDatabase
import com.example.weatherforecastapp.databinding.FragmentWeatherBinding
import com.example.weatherforecastapp.extensions.getImageResource
import com.example.weatherforecastapp.extensions.makeLongToast
import com.example.weatherforecastapp.extensions.makeShortToast
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.Calendar
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class WeatherFragment : Fragment() {

    private lateinit var binding: FragmentWeatherBinding
    private lateinit var weatherViewModel: WeatherViewModel
    private lateinit var favouriteLocationDao: FavouriteLocationDao

    var searchQuery: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            searchQuery = it.getString("searchQuery")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeatherBinding.inflate(layoutInflater)
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
            updateUI(searchQuery)
        }
        observeSearchWeatherData()
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

        binding.apply {
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
                insertLocation(favouriteLocation)

            }
        }
    }

    private fun insertLocation(favouriteLocation: FavouriteLocation) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val insertResult = favouriteLocationDao.insertFavoriteLocation(favouriteLocation)
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


    private fun updateUI(searchQuery: String?) {
        if (searchQuery != null) {
            weatherViewModel.getCurrentWeatherData(searchQuery)
        }
    }

}
