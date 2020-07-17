package com.mapmyfarm.mapmyfarm

import androidx.room.*

@Dao
interface FarmDao {
    @Query("SELECT * FROM farmclass")
    fun getAll(): List<FarmClass>

    @Query("SELECT * FROM farmclass WHERE id = :userID")
    fun loadById(userID: String): FarmClass

    @Query("SELECT * FROM farmclass WHERE id IN (:userIds)")
    fun loadAllByIds(userIds: List<String>): List<FarmClass>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg farm: FarmClass)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(farms: List<FarmClass>)

    @Update
    fun updateFarms(vararg farm: FarmClass)

    @Delete
    fun delete(farm: FarmClass)

    @Query("DELETE FROM farmclass WHERE id = :delID")
    fun deleteByID(delID: String)

}