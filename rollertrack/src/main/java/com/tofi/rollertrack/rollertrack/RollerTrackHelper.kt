package com.tofi.rollertrack.rollertrack

import android.content.res.Configuration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSmoothScroller
import android.support.v7.widget.RecyclerView

/**
 * Created by Derek on 04/03/2018.
 * Helper for pairing a [RollerTrack] with a [RecyclerView]. Monitors scrolling of the list and
 * updates the [RollerTrack] if needed. Also handles any clicks to the [RollerTrack].
 * @param listItems The list of items used to populate the [RecyclerView] adapter.
 */
class RollerTrackHelper<T>(private val listItems: List<T>) {

    companion object {
        private const val MINIMUM_TRACK_ITEMS_THRESHOLD = 5
    }

    /**
     * Determines the directions for [RollerTrack] to move in
     */
    enum class DIRECTION { NEXT, PREVIOUS }

    // RecyclerView elements
    private var recyclerView: RecyclerView? = null
    private var layoutManager: LinearLayoutManager? = null
    private var smoothScroller: LinearSmoothScroller? = null

    // Roller track elements
    private var rollerTrack: RollerTrack? = null
    private var trackItems: List<RollerTrackItem<T>> = mutableListOf()
    private var currentTrackItem: RollerTrackItem<T>? = null

    // Keeps track of whether the list is scrolling due to a track item click rather than user dragging
    private var clickScrolling: Boolean = false

    // Keeps track of whether the roller track should be shown
    private var showRollerTrack: Boolean = false

    // Monitors scrolling and notifies roller track of any updates needed
    private val rollerTrackScrollListener = object: RecyclerView.OnScrollListener() {

        // The previous item that was in focus
        private var previousFirstVisibleItem: Int = 0

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            // Don't process horizontal scrolls or any scrolls with no vertical change
            if (dx > 0 || dy == 0) {
                return
            }

            val firstVisibleItem = layoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
            val lastVisibleItem = layoutManager?.findLastCompletelyVisibleItemPosition() ?: 0

            /*
             * If we have reached the end of the list, fire an event for the last view as the new focus.
             * This is needed as the last view may never become focus if it cannot scroll high enough
             * to be the first fully visible view.
             */
            if (lastVisibleItem >= 0 && lastVisibleItem == recyclerView.adapter.itemCount - 1) {
                newItemFocus(lastVisibleItem)
                previousFirstVisibleItem = lastVisibleItem

            } else if (firstVisibleItem >= 0 && firstVisibleItem != previousFirstVisibleItem) {
                newItemFocus(firstVisibleItem)
                previousFirstVisibleItem = firstVisibleItem
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                clickScrolling = false
            }
        }
    }

    /**
     * Attach this helper to [recyclerView].
     * Currently only [LinearLayoutManager] with vertical scrolling is supported. The layout
     * manager must be set on the [RecyclerView] before calling this.
     * @param recyclerView The [RecyclerView] showing all the items [rollerTrack] is associated with
     * @param rollerTrack The [RollerTrack] associated with the [RecyclerView]
     * @param trackItems List of all [RollerTrackItem]s for the [rollerTrack]
     */
    fun attachToRecyclerView(recyclerView: RecyclerView,
                             rollerTrack: RollerTrack,
                             trackItems: List<RollerTrackItem<T>>) {

        // Reset helper if attaching to a new RecyclerView
        if (this.recyclerView != null && this.recyclerView != recyclerView) {
            detachFromRecyclerView()
            currentTrackItem = null
            clickScrolling = false
            showRollerTrack = false
        }

        this.recyclerView = recyclerView
        this.rollerTrack = rollerTrack
        this.trackItems = trackItems
        val layoutManager = recyclerView.layoutManager ?: throw UnsupportedLayoutManagerException("Layout manager on RecyclerView must be set")

        if (layoutManager !is LinearLayoutManager || layoutManager.orientation != LinearLayoutManager.VERTICAL) {
            throw UnsupportedLayoutManagerException("Only LinearLayoutManager with vertical orientation is supported")
        }

        showRollerTrack = trackItems.size >= MINIMUM_TRACK_ITEMS_THRESHOLD
                && recyclerView.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

        if (trackItems.isNotEmpty()) {
            currentTrackItem = trackItems[0]
        }

        if (showRollerTrack) {
            this.layoutManager = layoutManager

            if (smoothScroller == null) {
                smoothScroller = object: LinearSmoothScroller(recyclerView.context) {
                    override fun getVerticalSnapPreference(): Int {
                        return LinearSmoothScroller.SNAP_TO_END
                    }
                }
            }

            rollerTrack.trackItems = trackItems.toTypedArray()

            recyclerView.addOnScrollListener(rollerTrackScrollListener)
            rollerTrack.onTrackItemClickAction = this::handleTrackItemClicked
        }
    }

    /**
     * Call this if you need to manually detach this helper and any listeners from the
     * [RecyclerView] and [RollerTrack]
     */
    fun detachFromRecyclerView() {
        recyclerView?.removeOnScrollListener(rollerTrackScrollListener)
        rollerTrack?.onTrackItemClickAction = null

        recyclerView = null
        layoutManager = null
        rollerTrack = null
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

                when {
                    // Roll to beginning of list
                    itemPosition <= 0 -> setTrackGroup(0)

                    // Roll to end of list
                    itemPosition >= listItems.size - 1 -> setTrackGroup(trackItems.size - 1)

                    /*
                     * If the new item belongs to a new group, we check whether the group is before
                     * or after the current group and update the track accordingly.
                     */
                    it.trackItemName != currentTrackItem?.trackItemName -> updateTrackGroup(this@RollerTrackHelper.getNextTrackItemPosition(it))
                }

                currentTrackItem = newFocusTrackGroup
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
            smoothScroller?.targetPosition = listItems.indexOf(itemToScrollTo)
            layoutManager?.startSmoothScroll(smoothScroller)
        }
    }
}