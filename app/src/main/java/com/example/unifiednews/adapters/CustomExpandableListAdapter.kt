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
import com.bumptech.glide.Glide
import com.example.unifiednews.R
import com.example.unifiednews.data.RssFeedItem
import com.example.unifiednews.data.RssFilterItem
import com.example.unifiednews.managers.RssFeedStateManager
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
    var onChildMoreButtonClicked: ((String?, Int, String) -> Unit)? = null
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
        /*val folderNameTextView = view.findViewById<TextView>(R.id.Header)
        folderNameTextView.text =
*/
        val folderName = folderListArray[groupPosition]
        val folderSwitch = view.findViewById<Switch>(R.id.folderSwitch2)
        folderSwitch.visibility = View.VISIBLE
        folderSwitch.isFocusable = false
        folderSwitch.isClickable = true
        val bla = rssFeedStorage.getFolderSwitchBool(folderName)
        folderSwitch.isChecked = bla
        Log.d("bla", bla.toString())
        folderSwitch.setOnClickListener {
            Log.d("folderSwitch", folderSwitch.isChecked.toString())
            val list = rssFeedsMap[folderName]
            if (list != null) {
                for (str in list) {
                    RssFeedStateManager.setRssFeedState(str, folderSwitch.isChecked)
                    rssFeedStorage.setRssFeedState(str, folderSwitch.isChecked)
                    rssFeedStorage.setSwitchBoolToFolder(folderName, folderSwitch.isChecked)
                }
            }
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
        val item = rssFeedStorage.getFilterItem(getChild(groupPosition, childPosition) as String)
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
            Log.d("DELETETHIS SHIT", "BRÖ")
            onChildMoreButtonClicked?.invoke(item?.link, childPosition, folderName)
        }

        if (convertView != null) {
            Glide.with(convertView.context)
                .load(item?.iconLink)
                .into(image)
        }

        return view
        /*
        val view: View = if (convertView == null || convertView.tag != groupPosition) {
            // Inflate a new layout if we don't have a recycled view or if it's not tagged with the correct group position
            LayoutInflater.from(context).inflate(R.layout.layout_child_recyclerview, parent, false).apply {
                // Tag this view with the group position so we can identify it during recycling
                tag = groupPosition
            }
        } else {
            convertView
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.childRecyclerView)
        // Check if we need to initialize or update the RecyclerView adapter
        if (recyclerView.adapter == null || view.tag != groupPosition) {
            initializeRecyclerViewAdapter(recyclerView, groupPosition)
            lastChildBool = isLastChild
        }

        return view*/
        /* var view: View? = convertView
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
         return view ?: View(context) // Return the view or a default view to avoid null*/

        /*val view: View
        Log.d("COUNTER1", groupPos.toString() + " " + groupPosition)
        if (groupPosition == groupPos) {
            if (convertView != null) {
                Log.d("convertView", convertView.toString())
                return convertView
            }
            Log.d("ZEROVIEW", convertView.toString())
            return View(context).apply {
                layoutParams = ViewGroup.LayoutParams(0, 0) // Zero size

            }
        } else if (lastChildBool) {
            lastChildBool = false
        }
        if (convertView == null  ) {

            if (!lastChildBool) {
                view = LayoutInflater.from(context).inflate(R.layout.layout_child_recyclerview, parent, false)
                val recyclerView = view.findViewById<RecyclerView>(R.id.childRecyclerView)

                initializeRecyclerViewAdapter(recyclerView, groupPosition)
                lastChildBool = isLastChild
                Log.d("COUNTER2", lastChildBool.toString())
            } else {
                view = View(context).apply {
                    layoutParams = ViewGroup.LayoutParams(0, 0) // Zero size
                }
            }

        } else {

            view = convertView
        }
        if (lastChildBool) groupPos = groupPosition

        return view*/


    }

    private fun initializeRecyclerViewAdapter(recyclerView: RecyclerView, groupPosition: Int) {
        val rssFeedUrls = rssFeedsMap[folderListArray[groupPosition]] ?: listOf()
        val folderName = folderListArray[groupPosition]

        Log.d("SNELA", "Initializing RecyclerView for folder: $folderName with URLs: $rssFeedUrls")

        if (false) {
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
