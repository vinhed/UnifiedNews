package com.example.unifiednews.adapters;

import android.content.Context;
import android.util.Log
import android.util.LogPrinter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox
import android.widget.ExpandableListView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.unifiednews.R
import com.example.unifiednews.data.RssFeedItem
import com.example.unifiednews.data.RssFilterItem
import com.example.unifiednews.managers.RssFeedStateManager
import com.example.unifiednews.repository.RssFeedStorage
import com.example.unifiednews.ui.feed.SharedViewModel
import com.example.unifiednews.ui.filter.FilterViewModel
import kotlin.math.log

class CustomExpandableListAdapter(
    private val context: Context,
    private var folderList: Set<String>,
    private var rssFeedsMap: Map<String, List<String>>,
    private var filterViewModel: FilterViewModel,
    private val sharedViewModel: SharedViewModel,

) : BaseExpandableListAdapter(), RssFilterAdapter.OnItemRemovedListener {
    private var folderListArray = folderList.toTypedArray()
    var onChildMoreButtonClicked: ((String?, Int, String) -> Unit)? = null
    var onFolderLongPressed: ((String, Int) -> Unit)? = null
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
            convertView ?: inflater.inflate(R.layout.group_item, parent, false)
        val folderNameText = view.findViewById<TextView>(R.id.tvGroup)
        folderNameText.text = folderListArray[groupPosition]

        val folderName = folderListArray[groupPosition]
        val folderSwitch = view.findViewById<Switch>(R.id.folderSwitch2)
        folderSwitch.visibility = View.VISIBLE
        folderSwitch.isFocusable = false
        folderSwitch.isClickable = true

        folderSwitch.isChecked = filterViewModel.getFolderSwitchBool(folderName)
        folderSwitch.setOnClickListener {
            val list = rssFeedsMap[folderName]
            if (list != null) {
                for (str in list) {
                    RssFeedStateManager.setRssFeedState(str, folderSwitch.isChecked)
                    filterViewModel.setRssFeedState(str, folderSwitch.isChecked)
                    filterViewModel.setSwitchBoolToFolder(folderName, folderSwitch.isChecked)
                    sharedViewModel.notifyRssFeedChanged()
                }
            }
        }
        view.setOnClickListener {

            val listView = parent as ExpandableListView
            if (isExpanded) listView.collapseGroup(groupPosition)
            else listView.expandGroup(groupPosition, true)
        }
        view.setOnLongClickListener {
            onFolderLongPressed?.invoke(folderName, groupPosition)
            true
        }
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
        val item = filterViewModel.getFilterItem(getChild(groupPosition, childPosition) as String)
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater.inflate(R.layout.recycler_view_filter_item, null)

        val header: TextView = view.findViewById(R.id.Header)
        val description: TextView = view.findViewById(R.id.Description)
        val removeButton: ImageButton = view.findViewById(R.id.moreButton)
        removeButton.setImageResource(R.drawable.remove)
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)
        val image: ImageView = view.findViewById(R.id.imageView2)
        checkBox.isChecked = RssFeedStateManager.isRssFeedEnabled(item?.link)
        header.text = item?.header
        description.text = item?.link
        val folderName = folderListArray[groupPosition]
        removeButton.setOnClickListener {
            onChildMoreButtonClicked?.invoke(item?.link, childPosition, folderName)

            updateFolders(filterViewModel.getFoldersMap())
        }
        checkBox.setOnClickListener {
            RssFeedStateManager.setRssFeedState(description.text as String, checkBox.isChecked)
            filterViewModel.setRssFeedState(description.text as String, checkBox.isChecked)
            sharedViewModel.notifyRssFeedChanged()
        }

        if (convertView != null) {
            Glide.with(convertView.context)
                .load(item?.iconLink)
                .into(image)
        }

        return view


    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    fun updateFolders(map: Map<String, List<String>>) {
        rssFeedsMap = map
        folderList = rssFeedsMap.keys
        folderListArray = folderList.toTypedArray()
        notifyDataSetChanged()
    }

    override fun getGroupCount(): Int {
        return folderList.size
    }


    override fun onItemRemoved(rssFeedMap: Map<String, List<String>>) {
        rssFeedsMap = rssFeedMap
    }

}
