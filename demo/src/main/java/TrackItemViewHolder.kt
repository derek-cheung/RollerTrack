package com.tofi.rollertrack.demo

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.roller_track_row.view.*

/**
 * Created by Derek on 10/03/2018.
 * Displays a single [TrackItem] in a list.
 */
class TrackItemViewHolder(rootView: View): RecyclerView.ViewHolder(rootView) {

    fun bindData(trackItem: TrackItem) {
        itemView.text_title.text = trackItem.title
        itemView.text_description.text = trackItem.description
    }
}