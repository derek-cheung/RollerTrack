package com.tofi.rollertrack.rollertrack

import android.content.res.Configuration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

/**
 * Created by Derek on 04/03/2018.
 * Helper for pairing a [RollerTrack] with a [RecyclerView]. Monitors scrolling of the list and
 * updates the [RollerTrack] if needed. Also handles any clicks to the [RollerTrack].
 */
class RollerTrackHelper<T>(var listItems: MutableList<T>) {

    companion object {
        private const val MINIMUM_TRACK_ITEMS_THRESHOLD = 5
    }

    /**
     * Determines the directions for [RollerTrack] to move in
     */
    enum class DIRECTION { NEXT, PREVIOUS }

    private var recyclerView: RecyclerView? = null
    private var rollerTrack: RollerTrack? = null
    private var trackItems: MutableList<RollerTrackItem<T>> = mutableListOf()
    private var currentTrackItem: RollerTrackItem<T>? = null

    // Keeps track of whether the list is scrolling due to a track item click rather than user dragging
    private var clickScrolling: Boolean = false

    // Keeps track of whether the roller track should be shown
    private var showRollerTrack: Boolean = false

    fun attachToRecyclerView(recyclerView: RecyclerView,
                             rollerTrack: RollerTrack,
                             trackItems: MutableList<RollerTrackItem<T>>) {
        this.recyclerView = recyclerView
        this.rollerTrack = rollerTrack
        this.trackItems = trackItems
        val layoutManager = recyclerView.layoutManager

        showRollerTrack = trackItems.size >= MINIMUM_TRACK_ITEMS_THRESHOLD
                && recyclerView.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

        if (trackItems.isNotEmpty()) {
            currentTrackItem = trackItems[0]
        }

        if (layoutManager != null && layoutManager is LinearLayoutManager) {

            recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {

                // The previous item that was in focus
                private var previousFirstVisibleItem: Int = 0

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    // Don't process horizontal scrolls or any scrolls with no vertical change
                    if (dx > 0 || dy == 0) {
                        return
                    }

                    val firstVisibleItem = layoutManager.findFirstCompletelyVisibleItemPosition()
                    val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()

                    if (firstVisibleItem >= 0 && firstVisibleItem != previousFirstVisibleItem) {
                        newItemFocus(firstVisibleItem)
                        previousFirstVisibleItem = firstVisibleItem

                        /*
                         * If we have reached the end of the list, fire an event for the last view as the new focus.
                         * This is needed as the last view may never become focus if it cannot scroll high enough
                         * to be the first fully visible view.
                         */
                    } else if (lastVisibleItem >= 0 && lastVisibleItem == recyclerView.adapter.itemCount - 1) {
                        newItemFocus(lastVisibleItem)
                        previousFirstVisibleItem = lastVisibleItem
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        clickScrolling = false
                    }
                }
            })

            rollerTrack.onTrackItemClickAction = this::handleTrackItemClicked
        }
    }

    /**
     * Handles a new item gaining focus in the list. Checks whether the new focus belongs to a new
     * [RollerTrackItem], updating the [RollerTrack] if it does.
     * @param itemPosition The position of the newly focused item in the list
     */
    private fun processNewItemFocus(itemPosition: Int) {
        recyclerView?.let {
            val newFocusItem = listItems[itemPosition]
            var newFocusTrackGroup: RollerTrackItem<T>? = null

            // Find which group the new item belongs to
            trackItems.forEach loop@ { trackGroup ->
                if (trackGroup.trackItemData.contains(newFocusItem)) {
                    newFocusTrackGroup = trackGroup
                    return@loop
                }
            }

            newFocusTrackGroup?.let {
                /*
                 * If the new item belongs to a new group, we check whether the group is before or
                 * after the current group and update the track accordingly.
                 */
                if (it.trackItemName != currentTrackItem?.trackItemName) {

                    when {
                        // Roll to beginning of list
                        itemPosition <= 0 -> setTrackGroup(0)

                        // Roll to end of list
                        itemPosition >= recyclerView?.adapter?.itemCount ?: 0 -> setTrackGroup(trackItems.size - 1)

                        else -> updateTrackGroup(this@RollerTrackHelper.getNextTrackItemPosition(it))
                    }

                    currentTrackItem = newFocusTrackGroup
                }
            }
        }
    }

    /**
     * Set which track group should be showing
     * @param trackGroupPos Position of the track group in the list
     */
    private fun setTrackGroup(trackGroupPos: Int) {
        rollerTrack?.rollToTrackItem(trackGroupPos)
    }

    /**
     * Update the [RollerTrack] to the next position
     * @param direction Direction roller track should advance in
     */
    private fun updateTrackGroup(direction: DIRECTION) {
        when (direction) {
            DIRECTION.NEXT -> rollerTrack?.rollToNextTrackItem()
            DIRECTION.PREVIOUS -> rollerTrack?.rollToPreviousTrackItem()
        }
    }

    /**
     * Determines whether the new [RollerTrackItem] is before or after the current track item.
     * @param newItem The new track item
     * @return Whether the new track item is before or after
     */
    private fun getNextTrackItemPosition(newItem: RollerTrackItem<T>): DIRECTION {
        return if (newItem.trackItemName > currentTrackItem?.trackItemName ?: "") {
            DIRECTION.NEXT
        } else {
            DIRECTION.PREVIOUS
        }
    }

    /**
     * Called when the previous item is no longer fully visible in the list, with the focus
     * shifting to the new, first fully visible item.
     * @param itemPosition Position of the focused item in the [RecyclerView]
     */
    private fun newItemFocus(itemPosition: Int) {
        // Only process item if not scrolling due to track item click
        if (showRollerTrack && !clickScrolling) {
            processNewItemFocus(itemPosition)
        }
    }

    /**
     * Scroll to the clicked track item
     * @param trackItem The clicked track item
     */
    private fun handleTrackItemClicked(trackItem: RollerTrackItem<*>) {
        if (trackItem != currentTrackItem) {
            val itemToScrollTo = trackItem.trackItemData[0]
            clickScrolling = true
            currentTrackItem = trackItem as RollerTrackItem<T>
            recyclerView?.smoothScrollToPosition(listItems.indexOf(itemToScrollTo))
        }
    }
}