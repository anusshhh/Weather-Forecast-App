package com.example.weatherforecastapp.ui

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.weatherforecastapp.R
import com.example.weatherforecastapp.databinding.ActivityMainBinding
import com.example.weatherforecastapp.network.ApiClient
import com.example.weatherforecastapp.network.ApiResponse
import com.example.weatherforecastapp.repository.WeatherRepositoryImpl
import com.example.weatherforecastapp.viewmodel.WeatherViewModel
import com.example.weatherforecastapp.viewmodel.WeatherViewModelFactory
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var weatherViewModel: WeatherViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

    }

    private fun updateUI() {
        TODO("Not yet implemented")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val searchView = menu?.findItem(R.id.search)?.actionView as SearchView
        searchView.queryHint = "Search location"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d("ex", "onQueryTextSubmit: $query")
                val ex = query.toString()
                if (ex == "") {
                    Toast.makeText(this@MainActivity, "Invalid Location", Toast.LENGTH_SHORT).show()
                } else {
                    val navController = findNavController(R.id.nav_host_fragment)
                    val bundle = Bundle().apply {
                        putString("searchQuery", query.toString())
                    }
                    navController.navigate(R.id.weatherFragment, bundle)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })
        return super.onCreateOptionsMenu(menu)
    }
}
