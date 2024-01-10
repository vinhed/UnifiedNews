package com.example.unifiednews.adapters;

import android.content.Context;
import android.util.Log
import android.util.LogPrinter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.unifiednews.R
import com.example.unifiednews.repository.RssFeedStorage
import com.example.unifiednews.ui.feed.SharedViewModel
import kotlin.math.log

class CustomExpandableListAdapter(
    private val context: Context,
    private val folderList: Set<String>,
    private var rssFeedsMap: Map<String, List<String>>,
    private var rssFeedStorage: RssFeedStorage,
    private val sharedViewModel: SharedViewModel,
    private var groupPos: Int = -1,
    private var lastChildBool: Boolean = false


) : BaseExpandableListAdapter(), RssFilterAdapter.OnItemRemovedListener {
    private val folderListArray = folderList.toTypedArray()
    var onChildMoreButtonClicked: ((String, Int, String) -> Unit)? = null
    override fun getGroup(groupPosition: Int): Any {
        return folderListArray[groupPosition]
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view =
            convertView ?: inflater.inflate(R.layout.recycler_view_filter_item, parent, false)

        // Assuming the folder name is to be displayed in the 'Header' TextView
        val folderNameTextView = view.findViewById<TextView>(R.id.Header)
        folderNameTextView.text = folderListArray[groupPosition]

        val folderSwitch = view.findViewById<Switch>(R.id.folderSwitch)
        folderSwitch.visibility = View.VISIBLE
        folderSwitch.isFocusable = false
        folderSwitch.isClickable = true

        // You can still set an OnCheckedChangeListener to perform actions
        // when the switch's checked state changes

        // Hide or adjust other views (like CheckBox, ImageView, etc.) as per your need
        val checkBox = view.findViewById<CheckBox>(R.id.checkBox)
        val descriptionTextView = view.findViewById<TextView>(R.id.Description)
        val moreButton = view.findViewById<ImageButton>(R.id.moreButton)
        moreButton.visibility = View.GONE
        checkBox.visibility = View.GONE
        descriptionTextView.visibility = View.GONE

        return view
    }


    override fun getChildrenCount(groupPosition: Int): Int {
        return rssFeedsMap[folderListArray[groupPosition]]?.size ?: 0
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return rssFeedsMap[folderListArray[groupPosition]]!![childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {

        var view: View? = convertView
        if (groupPosition == groupPos) {
            if (view == null) {
                return View(context).apply {
                    layoutParams = ViewGroup.LayoutParams(0, 0)
                }
            } else return view

        } else if (lastChildBool) {
            lastChildBool = false

        }
        if (view == null) {
            Log.d("AVRFÖR", groupPosition.toString() + " "  + groupPos + " " + lastChildBool.toString())
            // Inflate a new layout if we don't have a recycled view
            view = LayoutInflater.from(context).inflate(R.layout.layout_child_recyclerview, parent, false)
            val recyclerView = view.findViewById<RecyclerView>(R.id.childRecyclerView)

            // Initialize RecyclerView Adapter only if it hasn't been initialized before
            if (recyclerView.adapter == null) {
                Log.d("is lastchild", isLastChild.toString() )
                lastChildBool = isLastChild
                initializeRecyclerViewAdapter(recyclerView, groupPosition)

            }
        }
        Log.d("KOLL", view.toString())
        if (lastChildBool) {
            groupPos = groupPosition
        }
        return view ?: View(context) // Return the view or a default view to avoid null
    }

    private fun initializeRecyclerViewAdapter(recyclerView: RecyclerView, groupPosition: Int) {
        val rssFeedUrls = rssFeedsMap[folderListArray[groupPosition]] ?: listOf()
        val folderName = folderListArray[groupPosition]

        Log.d("SNELA", "Initializing RecyclerView for folder: $folderName with URLs: $rssFeedUrls")

        val adapter = RssFilterAdapter(
            rssFeedUrls,
            rssFeedStorage,
            { sharedViewModel.notifyRssFeedChanged() },
            true,
            folderName,
            sharedViewModel
        ).apply {
            onMoreButtonClicked = { url, position ->
                onChildMoreButtonClicked?.invoke(url, position, folderName)
            }
        }
        updateFolders(rssFeedsMap)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }
    fun updateFolders(map: Map<String, List<String>>) {
        rssFeedsMap = map
    }
    override fun getGroupCount(): Int {
        return folderList.size
    }



    override fun onItemRemoved(rssFeedMap: Map<String, List<String>>) {
        Log.d("GETTING TO REMOVED", rssFeedMap.toString())
        rssFeedsMap = rssFeedMap
    }

}
