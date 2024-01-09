package com.example.unifiednews.adapters;

import android.content.Context;
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.unifiednews.R
import com.example.unifiednews.ui.feed.SharedViewModel

class CustomExpandableListAdapter(
    private val context: Context,
    private val folderList: Set<String>,
    private var rssFeedsMap: Map<String, List<String>>

) : BaseExpandableListAdapter(), RssFilterAdapter.OnItemRemovedListener {
    private val folderListArray = folderList.toTypedArray()
    private lateinit var sharedViewModel: SharedViewModel
    var onChildMoreButtonClicked: ((String, Int) -> Unit)? = null
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

        // Hide or adjust other views (like CheckBox, ImageView, etc.) as per your need
        val checkBox = view.findViewById<CheckBox>(R.id.checkBox)
        val imageView = view.findViewById<ImageView>(R.id.imageView2)
        val descriptionTextView = view.findViewById<TextView>(R.id.Description)
        val moreButton = view.findViewById<ImageButton>(R.id.moreButton)
        moreButton.visibility = View.GONE
        checkBox.visibility = View.GONE // For example, hiding the checkbox
        descriptionTextView.visibility = View.GONE
        // Set up other views as needed

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
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view =
            convertView ?: inflater.inflate(R.layout.layout_child_recyclerview, parent, false)

        // Get the RecyclerView from the inflated layout
        val recyclerView = view.findViewById<RecyclerView>(R.id.childRecyclerView)

        val rssFeedUrls = rssFeedsMap[folderListArray[groupPosition]]

        // Convert Java List to Kotlin List
        val kotlinRssFeedUrls = rssFeedUrls?.toList() ?: listOf()

        // Set up the RecyclerView with the RssFilterAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = RssFilterAdapter(kotlinRssFeedUrls) { sharedViewModel.notifyRssFeedChanged() }.apply {
            onMoreButtonClicked = onChildMoreButtonClicked
        }
        recyclerView.adapter = adapter

        return view
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
