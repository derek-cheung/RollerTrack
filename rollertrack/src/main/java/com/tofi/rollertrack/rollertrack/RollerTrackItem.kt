package com.tofi.rollertrack.rollertrack

/**
 * Created by Derek on 28/02/2018.
 * A track item used in [RollerTrack].
 * The [trackItemName] will be displayed in the [RollerTrack]. [trackItemData] is a list of all
 * data associated with this item.
 */
data class RollerTrackItem<T>(var trackItemName: String,
                              var trackItemData: MutableList<T>) {

    override fun equals(other: Any?): Boolean {
        return other != null && other is RollerTrackItem<*> && other.trackItemName == trackItemName
    }

    override fun hashCode(): Int {
        return trackItemName.hashCode()
    }
}