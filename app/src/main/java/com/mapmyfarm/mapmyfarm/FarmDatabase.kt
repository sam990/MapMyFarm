package com.mapmyfarm.mapmyfarm

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = arrayOf(FarmClass::class), version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class FarmDatabase : RoomDatabase() {
    abstract fun farmDao(): FarmDao
}
