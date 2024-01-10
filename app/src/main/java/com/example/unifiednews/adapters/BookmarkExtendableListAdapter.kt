package com.example.unifiednews.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.BaseExpandableListAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.example.unifiednews.data.RssFeedItem
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson
import com.example.unifiednews.R
import com.example.unifiednews.repository.RssFeedStorage
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookmarkExtendableListAdapter (
    private val context: Context,
    private val publishers: List<String>,
    private val feedItems: Map<String, List<RssFeedItem>>,
    private val webView: WebView,
    private val webViewTopBar: LinearLayout,
    private val webViewTopBarText: TextView,
    private val bottomSheet: LinearLayout,
    private val rssFeedStorage: RssFeedStorage
) : BaseExpandableListAdapter() {

    private fun parseDate(dateString: String): Date? {
        val formats = arrayOf(
            "EEE, dd MMM yyyy HH:mm:ss z",
            "EEE, dd MMM yyyy HH:mm:ss Z"
        )

        for (format in formats) {
            try {
                return SimpleDateFormat(format, Locale.ENGLISH).parse(dateString)
            } catch (e: ParseException) {
            }
        }

        return null
    }

    private fun stripHtml(html: String): String {
        return html.replace(Regex("<[^>]*>"), "").trim()
    }

    private fun formatDate(date: Date?): String {
        return if (date != null) {
            val formatter = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
            formatter.format(date)
        } else {
            ""
        }
    }

    override fun getGroup(groupPosition: Int): Any {
        return publishers[groupPosition]
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean,
                              convertView: View?, parent: ViewGroup?): View {
        val publisher = getGroup(groupPosition) as String
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater.inflate(R.layout.group_item, null)
        val tvGroup = view.findViewById<TextView>(R.id.tvGroup)
        tvGroup.text = publisher
        return view
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return feedItems[publishers[groupPosition]]?.size ?: 0
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return feedItems[publishers[groupPosition]]!![childPosition]
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean,
                              convertView: View?, parent: ViewGroup?): View {
        val item = getChild(groupPosition, childPosition) as RssFeedItem
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater.inflate(R.layout.recycler_view_feed_item, null)

        val header: TextView = view.findViewById(R.id.Header)
        val publisher: TextView = view.findViewById(R.id.Publisher)
        val description: TextView = view.findViewById(R.id.Description)
        val dateTime: TextView = view.findViewById(R.id.DateTime)
        val iconView: ImageView = view.findViewById(R.id.Icon)
        val container: ConstraintLayout = view.findViewById(R.id.Container)
        val bookmarkButton: ImageButton = view.findViewById(R.id.bookmarkButton)

        header.text = item.header
        publisher.text = item.publisher
        description.text = item.description?.let { stripHtml(it) }
        dateTime.text = formatDate(item.dateTime?.let { parseDate(it) })

        item.dateTime?.let {
            item.header?.let { it1 ->
                if(rssFeedStorage.isBookmarked(it1, it)) {
                    bookmarkButton.tag = "bookmarked"
                    bookmarkButton.setImageResource(R.drawable.bookmark_added)
                }
            }
        }

        bookmarkButton.setOnClickListener {
            if(bookmarkButton.tag == "not_bookmarked") {
                bookmarkButton.setImageResource(R.drawable.bookmark_added)
                bookmarkButton.tag = "bookmarked"
                rssFeedStorage.addBookmarkItem(item)
            } else {
                bookmarkButton.setImageResource(R.drawable.bookmark)
                bookmarkButton.tag = "not_bookmarked"
                rssFeedStorage.removeBookmarkItem(item)
            }
        }

        if (item.imageUrl != null) {
            if (convertView != null) {
                Log.d("getChildView", item.imageUrl)
                Glide.with(convertView.context)
                    .load(item.imageUrl)
                    .into(iconView)
            }
        }

        container.setOnClickListener {
            container.setBackgroundResource(R.drawable.rounded_panel_focus)
            item.link?.let { it1 -> webView.loadUrl(it1) }
            webView.visibility = View.VISIBLE
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            Handler(Looper.getMainLooper()).postDelayed({
                container.setBackgroundResource(R.drawable.rounded_panel)
            }, 500)
        }

        return view
    }

    override fun getGroupCount(): Int {
        return publishers.size
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }
}