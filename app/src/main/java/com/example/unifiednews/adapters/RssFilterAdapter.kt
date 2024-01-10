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
import com.example.unifiednews.managers.RssFeedStateManager
import com.example.unifiednews.repository.RssFeedFetcher
import com.example.unifiednews.repository.RssFeedStorage
import com.example.unifiednews.ui.feed.SharedViewModel
import okhttp3.OkHttpClient
import okhttp3.Request


class RssFilterAdapter(var rssFeedList: List<String>,
                       private var rssFeedStorage: RssFeedStorage,
                       private val notifyChange: () -> Unit,
                       private val isChildAdapter: Boolean = false,
                       private val folderName: String,
                       private val sharedViewModel: SharedViewModel) : RecyclerView.Adapter<RssFilterAdapter.ViewHolder>() {

    interface OnItemRemovedListener {
        fun onItemRemoved(rssFeedMap:Map<String, List<String>>)
    }
    init {
        rssFeedList.forEach { url ->
            RssFeedStateManager.setRssFeedState(url, rssFeedStorage.isRssFeedEnabled(url))
        }
        Log.d("CHECKEDSTATES", RssFeedStateManager.getCheckedStates().toString())
    }
    //private lateinit var rssFeedStorage: RssFeedStorage
    var onMoreButtonClicked: ((String, Int) -> Unit)? = null
    var onItemRemovedListener: OnItemRemovedListener? = null
    private fun fetchRssFeed(url: String, holder: ViewHolder) {
        RssFeedFetcher.fetchAndParseRssFeed(url) { rssFeedXml ->
            rssFeedXml?.channel?.items?.get(0)?.link.let { articleUrl ->
                (holder.itemView.context as? Activity)?.runOnUiThread {
                    holder.titleTextView.text = rssFeedXml?.channel?.title.toString()
                    holder.descriptionTextView.text = url
                }
                if(articleUrl == null) return@fetchAndParseRssFeed
                RssFeedFetcher.getPageIcon(articleUrl) { iconUrl ->
                    (holder.itemView.context as? Activity)?.runOnUiThread {
                        Log.d("RssFeed", iconUrl.toString())
                        Glide.with(holder.itemView.context)
                            .load(iconUrl)
                            .into(holder.imageView)
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
    fun toggleAllCheckboxes(checked: Boolean) {
        rssFeedList.forEach { url ->
            RssFeedStateManager.setRssFeedState(url, checked)
            rssFeedStorage.setRssFeedState(url, checked)
        }
        notifyDataSetChanged() // Notify adapter of data change
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
        rssFeedStorage = RssFeedStorage(parent.context.applicationContext as Application)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rssFeedUrl = rssFeedList[position]


        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = RssFeedStateManager.isRssFeedEnabled(rssFeedUrl)

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            RssFeedStateManager.setRssFeedState(rssFeedUrl, isChecked)
            rssFeedStorage.setRssFeedState(rssFeedUrl, isChecked)
            notifyChange()
            Log.d("ISITCHECKING", rssFeedStorage.getRssFeedState().toString())
            sharedViewModel.notifyRssFeedChanged()
        }
        fetchRssFeed(rssFeedUrl, holder)
        if (!isChildAdapter) {

            Log.d("Debug", "Binding view for URL: $rssFeedUrl at position $position")
        }
        if (isChildAdapter) {
            holder.moreButton.setImageResource(R.drawable.remove) // Icon for child adapter
        } else {
            holder.moreButton.setImageResource(R.drawable.more_vert) // Default icon
        }
        holder.moreButton.setOnClickListener {
            //display module _binding!!.moreModal
            //
            Log.d("APA", position.toString())
            onMoreButtonClicked?.invoke(rssFeedUrl, position)
            //removeItemAtPosition(position)
        }
    }

    override fun getItemCount() = rssFeedList.size

    fun removeItemAtPosition(position: Int, url: String) {
        val updatedList = rssFeedList.toMutableList()
        updatedList.removeAt(position)
        rssFeedList = updatedList
        notifyItemRemoved(position)
        rssFeedStorage.removeRssFeedUrl(url)
        rssFeedStorage.removeRssInFolders(url)
        val map = rssFeedStorage.getFoldersMap()
        Log.d("map", map.toString())
        onItemRemovedListener?.onItemRemoved(map)
    }
}