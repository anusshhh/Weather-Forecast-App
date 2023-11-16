package com.example.weatherforecastapp.model.db

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "favourite_locations")
data class FavouriteLocation(
    @PrimaryKey(autoGenerate = true)
    val id: Long=0,
    val name : String,
    val latitude: Double,
    val longitude: Double
)