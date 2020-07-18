package com.mapmyfarm.mapmyfarm

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = arrayOf(Farm::class, Harvest::class, FarmMapping::class, FarmHarvest::class), version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class FarmDatabase : RoomDatabase() {
    abstract fun farmDao(): FarmDao
    abstract fun harvestDao(): HarvestDao
    abstract fun farmMappingDao(): FarmMappingDao
    abstract fun farmHarvestDao(): FarmHarvestDao
}
