package com.example.weatherforecastapp.ui.feature

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.weatherforecastapp.R
import com.example.weatherforecastapp.databinding.ActivityMainBinding
import com.example.weatherforecastapp.extensions.makeShortToast


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setUpController()

    }

    private fun setUpController() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        //navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu, menu)
//
//        return super.onCreateOptionsMenu(menu)
//    }

//
//    private fun onFavoriteMenuItemClick() {
//        //  navController.popBackStack(R.id.favouriteLocationFragment, true)
//        navController.navigate(R.id.action_weatherFragment_to_favouriteLocationFragment)
//    }

    private fun handleSearchQuery(query: String?) {
        val inputQuery = query.toString()
        if (inputQuery.isEmpty()) {
            this.makeShortToast(getString(R.string.invalid_search_query))
        } else {
            val bundle = Bundle().apply {
                putString("searchQuery", query)
            }
            navController.popBackStack()
            navController.navigate(R.id.weatherFragment, bundle)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.popBackStack() || super.onSupportNavigateUp()
    }
}

