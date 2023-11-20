package com.example.weatherforecastapp.repository

import com.example.weatherforecastapp.dao.FavouriteLocationDao
import com.example.weatherforecastapp.model.db.FavouriteLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FavouriteLocationRepositoryImpl(private val favouriteLocationDao: FavouriteLocationDao) :
    IFavouriteLocationRepository {
    override suspend fun addFavoriteLocations(favouriteLocation:FavouriteLocation): Long {
        return favouriteLocationDao.addFavoriteLocation(favouriteLocation)
    }

    override fun getAllFavoriteLocations(): Flow<List<FavouriteLocation>> = flow {
        val result = favouriteLocationDao.getAllFavoriteLocations()
        emit(result)
    }

    override suspend fun getFavoriteLocationByCoordinates(latitude: Double, longitude: Double) : FavouriteLocation?{
        return favouriteLocationDao.getFavoriteLocationByCoordinates(longitude, latitude)
    }

    override suspend fun deleteFavouriteLocation(existingFavouriteLocation: FavouriteLocation) :Int {
        return favouriteLocationDao.deleteFavouriteLocation(existingFavouriteLocation)
    }

}
