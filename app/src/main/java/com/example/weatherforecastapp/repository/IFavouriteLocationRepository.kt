package com.example.weatherforecastapp.repository

import com.example.weatherforecastapp.model.db.FavouriteLocation
import kotlinx.coroutines.flow.Flow

interface IFavouriteLocationRepository {

    suspend fun addFavoriteLocations(favouriteLocation:FavouriteLocation):Long

    fun getAllFavoriteLocations(): Flow<List<FavouriteLocation>>

    suspend fun getFavoriteLocationByCoordinates(latitude : Double, longitude : Double) : FavouriteLocation?

    suspend fun deleteFavouriteLocation(existingFavouriteLocation:FavouriteLocation): Int
}