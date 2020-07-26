package com.mapmyfarm.mapmyfarm

import android.graphics.Color
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.text.FieldPosition
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


typealias OnHarvestCardClickListener = (String, String) -> Unit
typealias AddHarvestCardClickListener = (String) -> Unit

private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

class FarmViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    var farmView: CardView = view.findViewById(R.id.farm_cardview)
    private val farmIDView: TextView = view.findViewById(R.id.cardview_farmid)
    private val areaView: TextView = view.findViewById(R.id.cardview_area_value)
    private val expandView: LinearLayout = view.findViewById(R.id.expanded_view)

    private val addHarvestView: View = LayoutInflater.from(expandView.context).inflate(R.layout.add_harvest_cardview, expandView, false)
    private val harvestViews = ArrayList<View>()

    fun bindTo(farm: Farm) {
        farmIDView.text = farm.farmID.toString().padStart(3, '0')
        val areaVal: String = java.lang.String.format(Locale.getDefault(), "%.2f", farm.area)
        areaView.text = areaVal
    }

    fun showExpandedView(farmHarvest: FarmHarvest, listener: OnHarvestCardClickListener, addNewListener: AddHarvestCardClickListener) {

        val toInflate = farmHarvest.harvests.size - harvestViews.size
        val inflater = LayoutInflater.from(expandView.context)
        for (i in 0 until toInflate) {
            harvestViews.add(inflater.inflate(R.layout.harvest_cardview, expandView, false))
        }

        farmHarvest.harvests.forEachIndexed { i, harvest ->
            val harvestView = harvestViews[i]
            harvestView.findViewById<TextView>(R.id.cardview_harvest).text = harvest.crop
            harvestView.findViewById<TextView>(R.id.cardview_sowdate).text = sdf.format(harvest.sowingDate)
            // attach listener
            harvestView.setOnClickListener {
                listener(farmHarvest.farmID, harvest.id)
            }
            expandView.addView(harvestView)
        }

        // set listener
        addHarvestView.setOnClickListener {
            addNewListener(farmHarvest.farmID)
        }

        expandView.addView(addHarvestView)
        // show the view
        expandView.visibility = View.VISIBLE
    }


    fun hideExpandedView() {
        expandView.visibility = View.GONE
        // remove all child views
        expandView.removeAllViews()
    }

}