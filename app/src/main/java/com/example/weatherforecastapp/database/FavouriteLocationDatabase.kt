package com.example.weatherforecastapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.example.weatherforecastapp.dao.FavouriteLocationDao
import com.example.weatherforecastapp.model.db.FavouriteLocation

@Database(entities = [FavouriteLocation::class], version = 1, exportSchema = false)
abstract class FavouriteLocationDatabase : RoomDatabase() {

    abstract fun locationDao(): FavouriteLocationDao

    companion object {
        @Volatile
        private var INSTANCE: FavouriteLocationDatabase? = null

        fun getDatabase(context: Context): FavouriteLocationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FavouriteLocationDatabase::class.java,
                    "favourite_locations"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}