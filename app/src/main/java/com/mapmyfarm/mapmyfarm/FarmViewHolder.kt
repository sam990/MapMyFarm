package com.mapmyfarm.mapmyfarm

import android.graphics.Color
import android.text.format.DateUtils
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.FieldPosition
import java.text.SimpleDateFormat
import java.util.*


interface OnFarmCardClickListener{
    fun onCardClick(position: Int)
}

class FarmViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    private val farmIDView: TextView = view.findViewById(R.id.cardview_farmid)
    private val cropView: TextView = view.findViewById(R.id.cardview_crop_value)
    private var areaView: TextView = view.findViewById(R.id.cardview_area_value)
    var dateView: TextView = view.findViewById(R.id.cardview_date_value)


    fun bindTo(farm: FarmClass, listener: OnFarmCardClickListener ) {
        farmIDView.text = farm.farmID
        cropView.text = farm.crop

        val areaVal: String = java.lang.String.format(Locale.getDefault(), "%.2f", farm.area)
        areaView.text = areaVal
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        dateView.text = sdf.format(farm.sowingDate ?: "undated")
        itemView.setOnClickListener {
            listener.onCardClick(adapterPosition)
        }
    }
}