package com.tofi.rollertrack.demo

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

/**
 * Created by Derek on 10/03/2018.
 * Demo adapter for displaying a list of [TrackItem].
 */
class RollerTrackAdapter(private val trackItems: List<TrackItem>): RecyclerView.Adapter<TrackItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return TrackItemViewHolder(inflater.inflate(R.layout.roller_track_row, parent, false))
    }

    override fun onBindViewHolder(holder: TrackItemViewHolder, position: Int) {
        holder.bindData(trackItems[position])
    }

    override fun getItemCount(): Int = trackItems.size
}

