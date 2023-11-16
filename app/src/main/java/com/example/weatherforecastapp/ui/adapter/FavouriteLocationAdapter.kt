package com.example.weatherforecastapp.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherforecastapp.databinding.FavouriteLocationCardBinding
import com.example.weatherforecastapp.model.db.FavouriteLocation

class FavouriteLocationAdapter(private val onClick: (FavouriteLocation) -> Unit) :
    RecyclerView.Adapter<FavouriteLocationAdapter.FavouriteLocationViewHolder>() {

    private var favouriteLocationList: MutableList<FavouriteLocation> =
        emptyList<FavouriteLocation>().toMutableList()
    private lateinit var binding: FavouriteLocationCardBinding

    inner class FavouriteLocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val location = binding.tvLocationName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteLocationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = FavouriteLocationCardBinding.inflate(inflater, parent, false)
        return FavouriteLocationViewHolder(binding.root)
    }

    override fun getItemCount(): Int {
       return favouriteLocationList.size
    }

    override fun onBindViewHolder(holder: FavouriteLocationViewHolder, position: Int) {
        holder.location.text = favouriteLocationList[position].name

        holder.itemView.setOnClickListener{
            onClick.invoke(favouriteLocationList[position])
        }
    }

    @SuppressLint("NotifyDataSetChanged") // Entire list gets loaded through API.
    fun submitList(favouriteLocations: List<FavouriteLocation>) {
        favouriteLocationList = favouriteLocations.toMutableList()
        notifyDataSetChanged()
    }
}