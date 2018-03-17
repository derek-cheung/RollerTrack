package com.tofi.rollertrack.demo

import com.tofi.rollertrack.rollertrack.AlphabeticalTrackItem

/**
 * Created by Derek on 10/03/2018.
 * A demo track item model used to populate the list of track items.
 */
data class DemoTrackItem(var title: String = "",
                         var description: String = ""): AlphabeticalTrackItem {

    override fun getTrackItemName(): String = title
}