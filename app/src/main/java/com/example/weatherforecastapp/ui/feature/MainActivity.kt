package com.example.weatherforecastapp.ui

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.weatherforecastapp.R
import com.example.weatherforecastapp.databinding.ActivityMainBinding
import com.example.weatherforecastapp.extensions.makeShortToast
import com.example.weatherforecastapp.viewmodel.WeatherViewModel


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root


        setContentView(view)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        navController = findNavController(R.id.nav_host_fragment)

        val searchView = menu?.findItem(R.id.search)?.actionView as SearchView
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
        return super.onCreateOptionsMenu(menu)
    }


    private fun onFavoriteMenuItemClick() {
        navController.navigate(R.id.favouriteLocationFragment)
    }

    private fun handleSearchQuery(query: String?) {
        val inputQuery = query.toString()
        if (inputQuery.isEmpty()) {
            this.makeShortToast(getString(R.string.invalid_search_query))
        } else {
            val bundle = Bundle().apply {
                putString("searchQuery", query)
            }
            navController.navigate(R.id.weatherFragment, bundle)
        }
    }
}

