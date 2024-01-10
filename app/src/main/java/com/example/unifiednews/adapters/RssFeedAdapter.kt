package com.example.unifiednews.adapters

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.unifiednews.R
import com.example.unifiednews.data.RssFeedItem
import com.example.unifiednews.repository.RssFeedStorage
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RssFeedAdapter(
    private var rssFeedItems: List<RssFeedItem>,
    private val webView: WebView,
    private val webViewTopBar: LinearLayout,
    private val webViewTopBarText: TextView,
    private val bottomSheet: LinearLayout,
    private val rssFeedStorage: RssFeedStorage,
) : RecyclerView.Adapter<RssFeedAdapter.ViewHolder>() {

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

    fun updateAndSortData(newItems: List<RssFeedItem>) {
        rssFeedItems = newItems
        notifyDataSetChanged()
    }

    fun clearData() {
        rssFeedItems = emptyList()
        notifyDataSetChanged()
    }

    private fun formatDate(date: Date?): String {
        return if (date != null) {
            val formatter = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
            formatter.format(date)
        } else {
            ""
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val header: TextView = view.findViewById(R.id.Header)
        val publisher: TextView = view.findViewById(R.id.Publisher)
        val description: TextView = view.findViewById(R.id.Description)
        val dateTime: TextView = view.findViewById(R.id.DateTime)
        val iconView: ImageView = view.findViewById(R.id.Icon)
        val container: ConstraintLayout = view.findViewById(R.id.Container)
        val bookmarkButton: ImageButton = view.findViewById(R.id.bookmarkButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_feed_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = rssFeedItems[position]
        holder.header.text = item.header
        holder.publisher.text = item.publisher
        holder.description.text = item.description?.let { stripHtml(it) }
        holder.dateTime.text = formatDate(item.dateTime?.let { parseDate(it) })

        item.dateTime?.let {
            item.header?.let { it1 ->
                if(rssFeedStorage.isBookmarked(it1, it)) {
                    holder.bookmarkButton.tag = "bookmarked"
                    holder.bookmarkButton.setImageResource(R.drawable.bookmark_added)
                }
            }
        }

        holder.bookmarkButton.setOnClickListener {
            if(holder.bookmarkButton.tag == "not_bookmarked") {
                holder.bookmarkButton.setImageResource(R.drawable.bookmark_added)
                holder.bookmarkButton.tag = "bookmarked"
                rssFeedStorage.addBookmarkItem(item)
            } else {
                holder.bookmarkButton.setImageResource(R.drawable.bookmark)
                holder.bookmarkButton.tag = "not_bookmarked"
                rssFeedStorage.removeBookmarkItem(item)
            }
        }

        if (item.imageUrl != null) {
            Glide.with(holder.itemView.context)
                .load(item.imageUrl)
                .into(holder.iconView)
        }

        holder.container.setOnClickListener {
            holder.container.setBackgroundResource(R.drawable.rounded_panel_focus)
            item.link?.let { it1 -> webView.loadUrl(it1) }
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            Handler(Looper.getMainLooper()).postDelayed({
                holder.container.setBackgroundResource(R.drawable.rounded_panel)
            }, 500)
        }
    }

    override fun getItemCount() = rssFeedItems.size

}