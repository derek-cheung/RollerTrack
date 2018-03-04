package com.tofi.rollertrack.rollertrack

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.tofi.rollertrack.R

/**
 * Created by Derek on 28/03/2018.
 * This view displays a vertical track of items that can roll forwards or backwards between the items. Usually
 * this will be used with another view that can scroll with the track, being updated as the other view scrolls.
 * The items can be any list of string values. As an item rolls from one to another, animations for shrinking
 * the previously selected item and enlarging the newly selected item will play.
 */
class RollerTrack @JvmOverloads constructor(context: Context,
                                            attributes: AttributeSet? = null,
                                            defStyle: Int = 0):
        View(context, attributes, defStyle) {

    companion object {

        private const val INVALID_TRACK_ITEM = -1

        val CURRENT_TRACK_ITEM_KEY = "CurrentTrackItem"
        val PREVIOUS_TRACK_ITEM_KEY = "PreviousTrackItem"

        // Default values for this view
        private const val DEFAULT_TRACK_STROKE_WIDTH = 6
        private const val DEFAULT_TEXT_STROKE_WIDTH = 2
        private const val DEFAULT_ANIMATION_DURATION = 300
    }

    // List of all items to be placed in the track
    var trackItems: Array<RollerTrackItem<*>>? = null
    set(value) {
        if (value != null) {
            trackItemPositions = Array(value.size, { Rect() })
            measureTrackItemTextBounds(value[0].trackItemName)
        }

        field = value
    }

    // List of area bounds of all the track items in this view
    private var trackItemPositions: Array<Rect>? = null

    // Positions of the previous and the current track items
    private var previousTrackItem: Int = INVALID_TRACK_ITEM
    private var currentTrackItem: Int = INVALID_TRACK_ITEM

    // Objects for styling the view elements
    private var trackPaint = Paint()
    private var trackItemPaint = Paint()
    private var currentTrackItemTextStyle = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    private var backgroundTrackItemTextStyle = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

    // Calculated text bounds for a track item
    private var trackItemTextBounds = Rect()

    // Controls the animation for the track item text changes
    private var trackItemSizeChangeAnimator: ValueAnimator? = null

    // Dimensions for view elements
    private var trackWidth: Int = 0
    private var trackHeight:Int = 0

    // The default height of a background track item
    private var backgroundTextHeight = -1

    // Values for track item text sizes and the difference
    private var currentTrackItemTextSize: Int = 0
    private var backgroundTrackItemTextSize: Int = 0

    private val trackItemSizeDifference: Int
    get() { return currentTrackItemTextSize - backgroundTrackItemTextSize }

    init {
        if (isInEditMode) {
            trackItems = arrayOf()
        }

        trackPaint.isAntiAlias = true
        trackPaint.strokeWidth = DEFAULT_TRACK_STROKE_WIDTH.toFloat()
        trackPaint.color = Color.BLACK

        trackItemPaint.isAntiAlias = true
        trackItemPaint.strokeWidth = DEFAULT_TEXT_STROKE_WIDTH.toFloat()
        trackItemPaint.textSize = backgroundTrackItemTextSize.toFloat()
        trackItemPaint.color = Color.RED
        trackItemPaint.textAlign = Paint.Align.CENTER

        currentTrackItemTextSize = resources.getDimensionPixelOffset(R.dimen.default_current_track_item_text_size)
        backgroundTrackItemTextSize = resources.getDimensionPixelOffset(R.dimen.default_background_track_item_text_size)
    }

    override fun onSaveInstanceState(): Parcelable {
        super.onSaveInstanceState()
        val savedState = Bundle()
        savedState.putInt(CURRENT_TRACK_ITEM_KEY, currentTrackItem)
        savedState.putInt(PREVIOUS_TRACK_ITEM_KEY, previousTrackItem)
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        previousTrackItem = (state as Bundle).getInt(PREVIOUS_TRACK_ITEM_KEY)
        currentTrackItem = state.getInt(CURRENT_TRACK_ITEM_KEY)
        super.onRestoreInstanceState(state)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        trackWidth = width
        trackHeight = height
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        trackItems?.let {
            if (it.isEmpty()) { return }

            // Draw the vertical track lines
            canvas.drawLine(paddingLeft.toFloat(), paddingTop.toFloat(), paddingLeft.toFloat(),
                    (trackHeight - paddingBottom).toFloat(), trackPaint)
            canvas.drawLine((trackWidth - paddingRight).toFloat(), paddingTop.toFloat(),
                    (trackWidth - paddingRight).toFloat(), (trackHeight - paddingBottom).toFloat(), trackPaint)

            /*
             * singleHeight = the height of a single track item
             * startHeight = the height to start drawing a track item at
             */
            val dataSize = it.size
            val singleHeight = (trackHeight - paddingBottom) / dataSize
            var startHeight = paddingTop

            for (i in 0 until dataSize) {
                val trackItemSizeChangeAnimator = this.trackItemSizeChangeAnimator
                val data = it[i].trackItemName
                var height = startHeight + singleHeight / 2

                if (i == currentTrackItem) {
                    // Need to animate text size increase if the animator is running
                    if (trackItemSizeChangeAnimator != null && trackItemSizeChangeAnimator.isRunning) {

                        trackItemPaint.textSize = backgroundTrackItemTextSize +
                                trackItemSizeDifference * trackItemSizeChangeAnimator.animatedFraction

                    } else {

                        trackItemPaint.textSize = currentTrackItemTextSize.toFloat()
                    }

                    trackItemPaint.typeface = currentTrackItemTextStyle
                    measureTrackItemTextBounds(data)
                    height += (trackItemTextBounds.height() - backgroundTextHeight) / 2

                } else {
                    // Need to animate text size decrease if the animator is running
                    if (i == previousTrackItem && trackItemSizeChangeAnimator != null
                            && trackItemSizeChangeAnimator.isRunning) {

                        trackItemPaint.textSize = currentTrackItemTextSize -
                                trackItemSizeDifference * trackItemSizeChangeAnimator.animatedFraction

                    } else {

                        trackItemPaint.textSize = backgroundTrackItemTextSize.toFloat()
                    }

                    trackItemPaint.typeface = backgroundTrackItemTextStyle
                    measureTrackItemTextBounds(data)
                }

                canvas.drawText(data, (trackWidth / 2).toFloat(), height.toFloat(), trackItemPaint)

                trackItemPositions?.let {
                    // Add the area bounds of the new track item to the list
                    val trackItemPosition = it[i]
                    trackItemPosition.set(paddingLeft, startHeight, trackWidth - paddingRight,
                            startHeight + singleHeight)
                }

                startHeight += singleHeight
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {

            // Loop through all track item bounds to see if any have been clicked
            trackItemPositions?.let {
                it.forEachIndexed { index, rect ->
                    val trackItemPosition = it[index]

                    if (trackItemPosition.contains(event.x.toInt(), event.y.toInt() + paddingTop)) {
                        previousTrackItem = currentTrackItem
                        currentTrackItem = index
                        invalidate()

                        //callback.onTrackItemClicked(it[index])
                    }
                }
            }
        }

        return true
    }


    /**
     * Rolls forward to the next track item
     */
    fun rollToNextTrackItem() {
        trackItems?.let {

            if (currentTrackItem < it.size) {
                startTrackItemSizeChangeAnimation()
                previousTrackItem = currentTrackItem
                currentTrackItem++
            }
        }
    }

    /**
     * Rolls backward to the previous track item
     */
    fun rollToPreviousTrackItem() {
        trackItems?.let {

            if (currentTrackItem > 0) {
                startTrackItemSizeChangeAnimation()
                previousTrackItem = currentTrackItem
                currentTrackItem--
            }
        }
    }

    /**
     * Roll to a specified track item. In order to appear smooth, the track will roll to all the
     * items between current track item and specified track item.
     * @param trackItemPos The track item position to roll to
     */
    fun rollToTrackItem(trackItemPos: Int) {
        trackItems?.let {
            if (trackItemPos < it.size && trackItemPos >= 0) {

                if (trackItemPos > currentTrackItem) {
                    val numberOfRolls = trackItemPos - currentTrackItem

                    for (i in 0 until numberOfRolls) {
                        rollToNextTrackItem()
                    }

                } else if (trackItemPos < currentTrackItem) {
                    val numberOfRolls = currentTrackItem - trackItemPos

                    for (i in 0 until numberOfRolls) {
                        rollToPreviousTrackItem()
                    }
                }
            }
        }
    }

    /**
     * Measure and store the text bounds of a track item
     * @param trackItem The track item to measure
     */
    private fun measureTrackItemTextBounds(trackItem: String) {
        trackItemPaint.getTextBounds(trackItem, 0, trackItem.length, trackItemTextBounds)

        if (backgroundTextHeight < 0) {
            backgroundTextHeight = trackItemTextBounds.height()
        }
    }

    /**
     * Begin the track item size change animation
     */
    private fun startTrackItemSizeChangeAnimation() {
        trackItemSizeChangeAnimator = ValueAnimator.ofInt(0, trackItemSizeDifference)
        trackItemSizeChangeAnimator?.duration = DEFAULT_ANIMATION_DURATION.toLong()
        trackItemSizeChangeAnimator?.addUpdateListener({ invalidate() })
        trackItemSizeChangeAnimator?.start()
    }
}