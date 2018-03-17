package com.tofi.rollertrack.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.tofi.rollertrack.rollertrack.RollerTrack
import com.tofi.rollertrack.rollertrack.RollerTrackHelper
import com.tofi.rollertrack.rollertrack.RollerTrackItem
import kotlinx.android.synthetic.main.roller_track_activity.*
import org.json.JSONObject
import java.nio.charset.Charset

/**
 * Created by Derek on 10/03/2018.
 * Demo activity showing a list of track items and a [RollerTrack] for it.
 */
class RollerTrackActivity: AppCompatActivity() {

    private lateinit var rollerTrackHelper: RollerTrackHelper<TrackItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.roller_track_activity)

        val trackItems = loadTrackItems().sortedBy { it.title }
        val rollerTrackItems = generateRollerTrackItems(trackItems)

        val adapter = RollerTrackAdapter(trackItems)
        list_track_items.adapter = adapter

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        list_track_items.layoutManager = layoutManager

        rollerTrackHelper = RollerTrackHelper(trackItems)
        rollerTrackHelper.attachToRecyclerView(list_track_items, roller_track, rollerTrackItems)
    }

    private fun loadTrackItems(): List<TrackItem> {
        val trackItems = mutableListOf<TrackItem>()

        try {
            val input = assets.open("data.json")
            val size = input.available()
            val buffer = ByteArray(size)
            input.read(buffer)
            input.close()

            val json = buffer.toString(Charset.forName("UTF-8"))
            val jsonObject = JSONObject(json)
            val jsonArray = jsonObject.getJSONArray("data")

            for (i in 0 until jsonArray.length()) {
                val trackItem = TrackItem()
                val trackObject = jsonArray.getJSONObject(i)
                trackItem.title = trackObject.optString("title", "")
                trackItem.description = trackObject.optString("description", "")
                trackItems.add(trackItem)
            }

        } catch (exc: Exception) {}

        return trackItems
    }

    private fun generateRollerTrackItems(trackItems: List<TrackItem>): List<RollerTrackItem<TrackItem>> {
        val rollerTrackItems = mutableListOf<RollerTrackItem<TrackItem>>()

        var currentRollerTrackItemData: MutableList<TrackItem> = mutableListOf()
        var currentRollerTrackItem: RollerTrackItem<TrackItem> = RollerTrackItem("", currentRollerTrackItemData)
        trackItems.forEach {
            val firstLetter = it.title.substring(0, 1)
            if (currentRollerTrackItem.trackItemName != firstLetter) {

                currentRollerTrackItemData = mutableListOf()
                currentRollerTrackItemData.add(it)
                currentRollerTrackItem = RollerTrackItem(firstLetter, currentRollerTrackItemData)
                rollerTrackItems.add(currentRollerTrackItem)

            } else {
                currentRollerTrackItemData.add(it)
            }
        }

        return rollerTrackItems
    }
}