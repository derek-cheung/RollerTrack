# RollerTrack
This library allows attaching a roller track to a RecyclerView. The roller track provides faster navigation and visualisation for users 
facing long lists. It is designed to work with sorted lists of related content with fixed, pre-determined datasets. Example good use cases
are a catalogue of items grouped alphabetically or a list of events grouped by starting time.

## Usage
Start by adding your `RecyclerView` and `RollerTrack` to your layout file. You are free to position these however you want.

```
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="horizontal"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.tofi.rollertrack.rollertrack.RollerTrack
        android:id="@+id/roller_track"
        android:layout_width="0dp"
        android:layout_height="match_parent"
        android:layout_weight="0.2"
        android:paddingTop="10dp"
        android:paddingBottom="10dp"
        android:paddingLeft="10dp"
        android:paddingRight="10dp"/>

    <android.support.v7.widget.RecyclerView
        android:id="@+id/list_track_items"
        android:layout_width="0dp"
        android:layout_height="match_parent"
        android:layout_weight="0.8"
        android:clipToPadding="false"/>

</LinearLayout>
```

Next have your data model implement `AlphabeticalTrackItem`

```
data class DemoTrackItem(var title: String = "",
                         var description: String = ""): AlphabeticalTrackItem {

    override fun getTrackItemName(): String = title
}
```

Then sort your list of items, set up your `RecyclerView` normally and attach it to an `AlphabeticalTrackRollerHelper`

```
val trackItems: List<DemoTrackItem> = loadTrackItems().sortedBy { it.title }

val adapter = RollerTrackAdapter(trackItems)
list_track_items.adapter = adapter

val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
list_track_items.layoutManager = layoutManager

val rollerTrackHelper: AlphabeticalRollerTrackHelper<DemoTrackItem> = AlphabeticalRollerTrackHelper()
rollerTrackHelper.attachToRecyclerView(list_track_items, roller_track, trackItems)
```
