package com.mapmyfarm.mapmyfarm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter


class FarmAdapter(val listener: OnFarmCardClickListener) :
    ListAdapter<FarmClass, FarmViewHolder>(FarmClass.DIFF_CALLBACK) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FarmViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.farm_cardview, parent, false)
        return FarmViewHolder(v)
    }

    override fun onBindViewHolder(holder: FarmViewHolder, position: Int) {
        val item: FarmClass = getItem(position)
        holder.bindTo(item, listener)
    }

}
