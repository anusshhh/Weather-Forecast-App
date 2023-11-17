package com.example.weatherforecastapp.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherforecastapp.R
import com.example.weatherforecastapp.databinding.FavouriteLocationCardBinding
import com.example.weatherforecastapp.databinding.WeatherForecastCardBinding
import com.example.weatherforecastapp.extensions.getImageResource
import com.example.weatherforecastapp.model.api.Day
import com.example.weatherforecastapp.model.api.WeatherData
import com.example.weatherforecastapp.utils.DateUtils
import com.example.weatherforecastapp.utils.FormattingUtils

class WeatherForecastAdapter :
    RecyclerView.Adapter<WeatherForecastAdapter.WeatherForecastViewHolder>() {

    private var weatherForecastDataList: MutableList<Day> =
        emptyList<Day>().toMutableList()
    private lateinit var binding: WeatherForecastCardBinding

    inner class WeatherForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTime = binding.tvDatetime
        val temperature = binding.tvTemperature
        val weatherIcon = binding.ivWeatherIcon
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherForecastViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = WeatherForecastCardBinding.inflate(inflater, parent, false)
        return WeatherForecastViewHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return weatherForecastDataList.size
    }

    override fun onBindViewHolder(holder: WeatherForecastViewHolder, position: Int) {
        val weatherForecastData = weatherForecastDataList[position]
        val context = binding.root.context

        weatherForecastDataList[position].apply {
            val uri = FormattingUtils.formatIconName(weatherForecastData.icon)
            val imageResource = context.getImageResource(uri)
            val temperatureText = context.getString(
                R.string.temp_min_max,
                weatherForecastData.tempmax.toString(),
                weatherForecastData.tempmin.toString()
            )
            val dateTime = DateUtils.convertDate(weatherForecastData.datetime)

            if (imageResource != 0) {
                val res = ContextCompat.getDrawable(context, imageResource)
                holder.weatherIcon.setImageDrawable(res)
            } else {
                holder.weatherIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_unknown_weather
                    )
                )
            }
            holder.temperature.text = temperatureText
            holder.dateTime.text = dateTime
        }
    }
        @SuppressLint("NotifyDataSetChanged") // Entire list gets loaded through API.
        fun submitList(weatherForecastData: List<Day>) {
            weatherForecastDataList = weatherForecastData.toMutableList()
            notifyDataSetChanged()
        }
    }