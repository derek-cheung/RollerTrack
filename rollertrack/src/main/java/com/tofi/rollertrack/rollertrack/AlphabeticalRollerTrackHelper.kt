package com.tofi.rollertrack.rollertrack

/**
 * Created by Derek on 17/03/2018.
 * See [RollerTrackHelper]
 * Automatically creates [RollerTrackItem]s in an alphabetical list based on the
 * [AlphabeticalTrackItem.getTrackItemName] supplied. The groups created will be based on the first
 * letter of each item.
 */
class AlphabeticalRollerTrackHelper<T: AlphabeticalTrackItem>: RollerTrackHelper<T>() {

    /**
     * Implementation from [RollerTrackHelper]
     * Generate the alphabetical [RollerTrackItem]s
     */
    override fun generateRollerTrackItems(listItems: List<T>): MutableList<RollerTrackItem<T>> {
        val rollerTrackItems = mutableListOf<RollerTrackItem<T>>()

        var currentRollerTrackItemData: MutableList<T> = mutableListOf()
        var currentRollerTrackItem: RollerTrackItem<T> = RollerTrackItem("", currentRollerTrackItemData)
        listItems.forEach {
            val firstLetter = it.getTrackItemName().substring(0, 1)
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