package com.example.weatherforecastapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.weatherforecastapp.databinding.FragmentWeatherBinding
import com.example.weatherforecastapp.model.WeatherData
import com.example.weatherforecastapp.network.ApiClient
import com.example.weatherforecastapp.network.ApiResponse
import com.example.weatherforecastapp.repository.WeatherRepositoryImpl
import com.example.weatherforecastapp.viewmodel.WeatherViewModel
import com.example.weatherforecastapp.viewmodel.WeatherViewModelFactory
import kotlinx.coroutines.launch

class WeatherFragment : Fragment() {

    private lateinit var binding: FragmentWeatherBinding
    private lateinit var weatherViewModel: WeatherViewModel

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
                        Log.e("TAG", "observeSearchWeatherData: Success", )
                        val weatherData = response.data
                        Log.d("Updated TAG", "onCreate: ${response.data}")
                        if (weatherData != null) {
                            updateUI(weatherData)
                        }
                    }

                    is ApiResponse.Error -> {
                        Log.e("TAG", "observeSearchWeatherData: Error", )
                    }

                    is ApiResponse.Loading -> {
                        Log.e("TAG", "observeSearchWeatherData: Loading", )
                    }
                }
            }
        }
    }

    fun updateUI(weatherData: WeatherData) {
        binding.apply {
            cityName.text = weatherData.resolvedAddress
            timestamp.text = weatherData.days[0].datetime
            val temp = weatherData.days[0].temp.toString()+"°"
            temperature.text = temp
            weatherTitle.text = weatherData.currentConditions.conditions
            tvHumidityValue.text = weatherData.currentConditions.humidity.toString()
            tvWindValue.text = weatherData.currentConditions.windspeed.toString()
            tvPrecipitationValue.text = weatherData.currentConditions.precip.toString()
        }
    }

    private fun updateUI(searchQuery: String?) {
        if (searchQuery != null) {
            weatherViewModel.getCurrentWeatherData(searchQuery)
        }
    }

}
