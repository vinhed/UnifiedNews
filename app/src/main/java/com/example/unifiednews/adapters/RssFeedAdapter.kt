package com.example.unifiednews.adapters

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.unifiednews.R
import com.example.unifiednews.data.RssFeedItem
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RssFeedAdapter(private var rssFeedItems: List<RssFeedItem>) : RecyclerView.Adapter<RssFeedAdapter.ViewHolder>() {

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

    fun updateData(newItems: List<RssFeedItem>) {
        rssFeedItems = newItems
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
        //val webView: WebView = view.findViewById(R.id.webView)
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

        if (item.imageUrl != null) {
            Glide.with(holder.itemView.context)
                .load(item.imageUrl)
                .into(holder.iconView)
        }

        holder.container.setOnClickListener {
            holder.container.setBackgroundResource(R.drawable.rounded_panel_focus)
            //item.link?.let { it1 -> holder.webView.loadUrl(it1) }
            //holder.webView.visibility = View.VISIBLE
            Handler(Looper.getMainLooper()).postDelayed({
                holder.container.setBackgroundResource(R.drawable.rounded_panel)
            }, 500)
        }
    }

    override fun getItemCount() = rssFeedItems.size

    // Additional methods to update the data set, etc.
}