package com.mapmyfarm.mapmyfarm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter


var expandedPosition = -1
var prevExpandedPosition = -1

class FarmAdapter(private val harvestCardClickListener: OnHarvestCardClickListener, private val addHarvestCardClickListener: AddHarvestCardClickListener) :
    ListAdapter<FarmHarvest, FarmViewHolder>(FarmHarvest.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FarmViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.farm_cardview, parent, false)
        return FarmViewHolder(v)
    }

    override fun onBindViewHolder(holder: FarmViewHolder, position: Int) {
        val item = getItem(position)
        holder.bindTo(item.farm)

        // check expanded
        val isExpanded = position == expandedPosition
//        val collapsed = position == prevExpandedPosition

//        if (collapsed) {
//            holder.hideExpandedView()
//        }

        holder.hideExpandedView()

        if (isExpanded) {
            holder.showExpandedView(item, harvestCardClickListener, addHarvestCardClickListener)
            prevExpandedPosition = position
        }


        holder.farmView.setOnClickListener {
            expandedPosition = if(isExpanded) -1 else position
            notifyItemChanged(prevExpandedPosition)
            notifyItemChanged(position)
        }
    }

}
