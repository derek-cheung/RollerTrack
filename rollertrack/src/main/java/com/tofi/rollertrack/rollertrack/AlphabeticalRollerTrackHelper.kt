package com.tofi.rollertrack.rollertrack

/**
 * Created by Derek on 17/03/2018.
 * See [RollerTrackHelper]
 * Automatically creates [RollerTrackItem]s in an alphabetical list based on the
 * [AlphabeticalTrackItem.getTrackItemName] supplied.
 *
 * Will throw an [IllegalArgumentException] if any [AlphabeticalTrackItem.getTrackItemName]
 * returns en empty String.
 *
 * @param maxNumberOfChars This indicates the maximum number of chars from the track item name that
 * will be used to create the group names. If a track item name has less chars than this, the full
 * name will be used as the group name.
 *
 * E.g. [maxNumberOfChars] is set to 4. Group name for 'table' would be 'tabl' and the group name
 * for 'tea' would be 'tea'.
 */
class AlphabeticalRollerTrackHelper<T: AlphabeticalTrackItem>(var maxNumberOfChars: Int = DEFAULT_MAX_NUMBER_OF_CHARS):
        RollerTrackHelper<T>() {

    companion object {
        private const val DEFAULT_MAX_NUMBER_OF_CHARS = 1
    }

    /**
     * Implementation from [RollerTrackHelper]
     * Generate the alphabetical [RollerTrackItem]s
     */
    override fun generateRollerTrackItems(listItems: List<T>): MutableList<RollerTrackItem<T>> {
        val rollerTrackItems = mutableListOf<RollerTrackItem<T>>()

        var currentRollerTrackItemData: MutableList<T> = mutableListOf()
        var currentRollerTrackItem: RollerTrackItem<T> = RollerTrackItem("", currentRollerTrackItemData)
        listItems.forEach {
            val trackItemName = it.getTrackItemName()

            if (trackItemName.isBlank()) {
                throw IllegalArgumentException("Track item names must not be empty")
            }

            val groupName = it.getTrackItemName().take(maxNumberOfChars)
            if (currentRollerTrackItem.trackItemName != groupName) {

                currentRollerTrackItemData = mutableListOf()
                currentRollerTrackItemData.add(it)
                currentRollerTrackItem = RollerTrackItem(groupName, currentRollerTrackItemData)
                rollerTrackItems.add(currentRollerTrackItem)

            } else {
                currentRollerTrackItemData.add(it)
            }
        }

        return rollerTrackItems
    }
}