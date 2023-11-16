package com.example.weatherforecastapp.ui.feature.favouritelocation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherforecastapp.R
import com.example.weatherforecastapp.dao.FavouriteLocationDao
import com.example.weatherforecastapp.dao.FavouriteLocationDao_Impl
import com.example.weatherforecastapp.database.FavouriteLocationDatabase
import com.example.weatherforecastapp.databinding.FragmentFavouriteLocationBinding
import com.example.weatherforecastapp.network.ApiClient
import com.example.weatherforecastapp.repository.FavouriteLocationRepositoryImpl
import com.example.weatherforecastapp.repository.WeatherRepositoryImpl
import com.example.weatherforecastapp.ui.adapter.FavouriteLocationAdapter
import com.example.weatherforecastapp.viewmodel.FavouriteLocationViewModel
import com.example.weatherforecastapp.viewmodel.FavouriteLocationViewModelFactory
import com.example.weatherforecastapp.viewmodel.WeatherViewModel
import com.example.weatherforecastapp.viewmodel.WeatherViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FavouriteLocationFragment : Fragment() {

    lateinit var favouriteLocationRecyclerView: RecyclerView
    lateinit var favouriteLocationAdapter: FavouriteLocationAdapter
    private lateinit var favouriteLocationDao: FavouriteLocationDao
    private lateinit var binding: FragmentFavouriteLocationBinding
    lateinit var favouriteLocationViewModel: FavouriteLocationViewModel
    lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavouriteLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        updateUI()
    }

    fun observeAllFavouriteLocations() {
        lifecycleScope.launch {
            favouriteLocationViewModel.favouriteLocations.observe(viewLifecycleOwner) {
                favouriteLocationAdapter.submitList(it)
            }
        }
    }

    private fun updateUI() {
        favouriteLocationDao =
            FavouriteLocationDatabase.getDatabase(requireContext()).locationDao()
        favouriteLocationViewModel = ViewModelProvider(
            this,
            FavouriteLocationViewModelFactory(FavouriteLocationRepositoryImpl(favouriteLocationDao))
        )[FavouriteLocationViewModel::class.java]

        favouriteLocationRecyclerView = binding.rvFavouriteLocations
        favouriteLocationRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        favouriteLocationAdapter = FavouriteLocationAdapter { location ->
            val bundle = Bundle().apply {
                putDouble("latitude", location.latitude)
                putDouble("longitude",location.longitude)
            }
            navController.navigate(R.id.action_favouriteLocationFragment_to_weatherFragment,bundle)
        }
        favouriteLocationRecyclerView.adapter = favouriteLocationAdapter
        observeAllFavouriteLocations()
    }
}

