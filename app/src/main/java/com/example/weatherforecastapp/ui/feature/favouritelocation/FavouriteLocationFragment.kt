package com.example.weatherforecastapp.ui.feature.favouritelocation

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherforecastapp.R
import com.example.weatherforecastapp.dao.FavouriteLocationDao
import com.example.weatherforecastapp.database.FavouriteLocationDatabase
import com.example.weatherforecastapp.databinding.FragmentFavouriteLocationBinding
import com.example.weatherforecastapp.model.db.FavouriteLocation
import com.example.weatherforecastapp.repository.FavouriteLocationRepositoryImpl
import com.example.weatherforecastapp.ui.adapter.FavouriteLocationAdapter
import kotlinx.coroutines.launch


class FavouriteLocationFragment : Fragment() {

    lateinit var favouriteLocationRecyclerView: RecyclerView
    lateinit var favouriteLocationAdapter: FavouriteLocationAdapter
    private lateinit var favouriteLocationDao: FavouriteLocationDao
    lateinit var favouriteLocationViewModel: FavouriteLocationViewModel
    lateinit var navController: NavController
    private var _binding: FragmentFavouriteLocationBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavouriteLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        favouriteLocationDao =
            FavouriteLocationDatabase.getDatabase(requireContext()).locationDao()
        favouriteLocationViewModel = ViewModelProvider(
            this,
            FavouriteLocationViewModelFactory(FavouriteLocationRepositoryImpl(favouriteLocationDao))
        )[FavouriteLocationViewModel::class.java]
        updateUI()
    }

    fun observeAllFavouriteLocations() {
        lifecycleScope.launch {
            favouriteLocationViewModel.favouriteLocations.observe(viewLifecycleOwner) { favouriteLocations ->
                if (favouriteLocations.isEmpty()) {
                    binding.tvNoFavourites.visibility = View.VISIBLE
                    favouriteLocationRecyclerView.visibility = View.GONE
                } else {
                    binding.tvNoFavourites.visibility = View.GONE
                    favouriteLocationRecyclerView.visibility = View.VISIBLE
                    favouriteLocationAdapter.submitList(favouriteLocations)
                }
            }
        }
    }

    private fun updateUI() {
        favouriteLocationRecyclerView = binding.rvFavouriteLocations
        favouriteLocationRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        favouriteLocationAdapter = FavouriteLocationAdapter(
            onClick = { location ->
                val bundle = Bundle().apply {
                    putDouble("latitude", location.latitude)
                    putDouble("longitude", location.longitude)
                    putBoolean("isFromFavourites", true)
                }
                navController.navigate(
                    R.id.action_favouriteLocationFragment_to_favouritesWeatherFragment,
                    bundle
                )
            },
            onDeleteClickListener = { favouriteLocation ->
                alertBuilderForDelete(favouriteLocation)
            }
        )
        favouriteLocationRecyclerView.adapter = favouriteLocationAdapter
        observeAllFavouriteLocations()
    }

    override fun onResume() {
        super.onResume()
        favouriteLocationViewModel.getAllFavouriteLocations()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun alertBuilderForDelete(favouriteLocation: FavouriteLocation) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setMessage(getString(R.string.delete_confirmation))
            .setCancelable(false)
            .setPositiveButton(
                getString(R.string.alert_builder_positive_response)
            ) { dialog, id ->
                favouriteLocationViewModel.deleteAndGetAllFavouriteLocations(favouriteLocation)
                dialog.cancel()
            }
            .setNegativeButton(
                getString(R.string.alert_builder_negative_response)
            ) { dialog, id ->
                dialog.cancel()
            }
        val alert: AlertDialog = builder.create()
        alert.show()
    }


}


