package com.example.unifiednews.adapters

import android.app.Activity
import android.app.Application
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.unifiednews.R
import com.example.unifiednews.data.RssFilterItem
import com.example.unifiednews.managers.RssFeedStateManager
import com.example.unifiednews.repository.RssFeedFetcher
import com.example.unifiednews.repository.RssFeedStorage
import com.example.unifiednews.ui.feed.SharedViewModel
import com.example.unifiednews.ui.filter.FilterViewModel
import okhttp3.OkHttpClient
import okhttp3.Request


class RssFilterAdapter(var rssFeedList: List<String>,
                       private var filterViewModel: FilterViewModel,
                       private val notifyChange: () -> Unit,
                       private val isChildAdapter: Boolean = false,
                       private val folderName: String,
                       private val sharedViewModel: SharedViewModel) : RecyclerView.Adapter<RssFilterAdapter.ViewHolder>() {

    interface OnItemRemovedListener {
        fun onItemRemoved(rssFeedMap:Map<String, List<String>>)
    }
    init {
        rssFeedList.forEach { url ->
            RssFeedStateManager.setRssFeedState(url, filterViewModel.isRssFeedEnabled(url))
        }
        Log.d("CHECKEDSTATES", RssFeedStateManager.getCheckedStates().toString())
    }

    var onMoreButtonClicked: ((String, Int) -> Unit)? = null
    var onItemRemovedListener: OnItemRemovedListener? = null

    private fun fetchFilterData(url: String, holder: ViewHolder) {

        val rssFilterItem = filterViewModel.getFilterItem(url)
        if(rssFilterItem != null) {
            holder.titleTextView.text = rssFilterItem.header
            holder.descriptionTextView.text = url
            Glide.with(holder.itemView.context)
                .load(rssFilterItem.iconLink)
                .into(holder.imageView)
        } else {
            RssFeedFetcher.fetchAndParseRssFeed(url) { rssFeedXml ->
                val rssFilterItemToAdd = RssFilterItem("", "", "")

                rssFeedXml?.channel?.items?.get(0)?.link.let { articleUrl ->
                    (holder.itemView.context as? Activity)?.runOnUiThread {
                        holder.titleTextView.text = rssFeedXml?.channel?.title.toString()
                        holder.descriptionTextView.text = url
                        rssFilterItemToAdd.header = rssFeedXml?.channel?.title.toString()
                        rssFilterItemToAdd.link = url
                    }
                    if(articleUrl == null) return@fetchAndParseRssFeed
                    RssFeedFetcher.getPageIcon(articleUrl) { iconUrl ->
                        (holder.itemView.context as? Activity)?.runOnUiThread {
                            if (iconUrl != null) {
                                rssFilterItemToAdd.iconLink = iconUrl
                            }
                            Glide.with(holder.itemView.context)
                                .load(iconUrl)
                                .into(holder.imageView)
                            filterViewModel.addFilterItem(rssFilterItemToAdd)
                        }
                    }
                }
            }
        }
    }

    private fun isRssFeed(responseBody: String?): Boolean {
        responseBody ?: return false
        return responseBody.contains("<rss") || responseBody.contains("<feed")
    }

    fun isValidRssFeedUrl(url: String, callback: (Boolean) -> Unit) {
        val urlRegex = "^(http://www\\.|https://www\\.|http://|https://)[a-zA-Z0-9-]+([-.]{1}[a-zA-Z0-9]+)*\\.[a-zA-Z]{2,5}(:[0-9]{1,5})?(/.*)?$".toRegex()

        if (!url.matches(urlRegex)) {
            callback(false)
            return
        }

        Thread {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful && isRssFeed(response.body?.string())) {
                        callback(true)
                    } else {
                        callback(false)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callback(false)
            }
        }.start()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)
        val imageView: ImageView = view.findViewById(R.id.imageView2)
        val titleTextView: TextView = view.findViewById(R.id.Header)
        val descriptionTextView: TextView = view.findViewById(R.id.Description)
        val moreButton: ImageButton = view.findViewById(R.id.moreButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_filter_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rssFeedUrl = rssFeedList[position]

        fetchFilterData(rssFeedUrl, holder)

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = RssFeedStateManager.isRssFeedEnabled(rssFeedUrl)

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            RssFeedStateManager.setRssFeedState(rssFeedUrl, isChecked)
            filterViewModel.setRssFeedState(rssFeedUrl, isChecked)
            sharedViewModel.notifyRssFeedChanged()
        }
        if (!isChildAdapter) {

        }
        if (isChildAdapter) {
            holder.moreButton.setImageResource(R.drawable.remove)
        } else {
            holder.moreButton.setImageResource(R.drawable.more_vert)
        }
        holder.moreButton.setOnClickListener {
            onMoreButtonClicked?.invoke(rssFeedUrl, position)
        }
    }

    override fun getItemCount() = rssFeedList.size

    fun removeItemAtPosition(position: Int, url: String) {
        val updatedList = rssFeedList.toMutableList()
        updatedList.removeAt(position)
        rssFeedList = updatedList
        notifyItemRemoved(position)
        filterViewModel.removeRssFeedUrl(url)
        filterViewModel.removeRssInFolders(url)
        val map = filterViewModel.getFoldersMap()
        onItemRemovedListener?.onItemRemoved(map)
        filterViewModel.removeFilterItem(url)
    }
}