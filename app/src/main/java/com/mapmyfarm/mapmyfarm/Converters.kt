package com.mapmyfarm.mapmyfarm

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromJSONArray(str: String?): ArrayList<LatLng> {
        if(str == null) {
            return ArrayList()
        }
        val retList = ArrayList<LatLng>()
        val jsonArray = JSONArray(str)
        for(i in 0 until jsonArray.length()) {
            val jsonObj = jsonArray.getJSONObject(i)
            retList.add(LatLng(jsonObj.getDouble("latitude"), jsonObj.getDouble("longitude") ))
        }
        return retList
    }

    @TypeConverter
    fun listToJSONArray(list: ArrayList<LatLng>?): String? {
        val jsonArray = JSONArray()
        if (list == null) {
            return null
        }
        for (loc in list) {
            val jsonObj = JSONObject()
            with(jsonObj){
                put("latitude", loc.latitude)
                put("longitude", loc.longitude)
            }
            jsonArray.put(jsonObj)
        }
        return jsonArray.toString()
    }


/*
    @TypeConverter
    fun jSONArrayToDoubleList(str: String?): ArrayList<Double> {
        if(str == null) {
            return ArrayList()
        }
        val retList = ArrayList<Double>()
        val jsonArray = JSONArray(str)
        for(i in 0 until jsonArray.length()) {
            retList.add(jsonArray.getDouble(i))
        }
        return retList
    }
    @TypeConverter
    fun doubleListTOJSONArray(list: ArrayList<Double>?): String?{
        if(list == null) {
            return null
        }
        val jsonArray = JSONArray()
        for (loc in list) {
            jsonArray.put(loc)
        }
        return jsonArray.toString()
    }
*/


}
