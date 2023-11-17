package com.example.weatherforecastapp.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherforecastapp.model.db.FavouriteLocation
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteLocationDao {
    @Insert
    suspend fun addFavoriteLocation(location: FavouriteLocation): Long

    @Query("SELECT * FROM favourite_locations")
    suspend fun getAllFavoriteLocations(): List<FavouriteLocation>

    @Delete
    suspend fun deleteFavouriteLocation(location: FavouriteLocation) : Int

    @Query("DELETE FROM favourite_locations")
    suspend fun deleteAllFavouriteLocations()

    @Query("SELECT * FROM favourite_locations WHERE longitude = :longitude AND latitude = :latitude LIMIT 1")
    suspend fun getFavoriteLocationByCoordinates(
        longitude: Double,
        latitude: Double
    ): FavouriteLocation?

}