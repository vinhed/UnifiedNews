package com.example.unifiednews.ui.feed

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.unifiednews.R
import com.example.unifiednews.adapters.RssFeedAdapter
import com.example.unifiednews.data.RssFeedItem
import com.example.unifiednews.databinding.FragmentFeedBinding
import com.example.unifiednews.repository.RssFeed
import com.example.unifiednews.repository.RssFeedFetcher
import com.example.unifiednews.repository.RssFeedStorage
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private lateinit var rssFeedStorage: RssFeedStorage

    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        rssFeedStorage = RssFeedStorage(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val feedViewModel = ViewModelProvider(this)[FeedViewModel::class.java]
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

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

    private fun sortAndUpdateList(items: MutableList<RssFeedItem>, adapter: RssFeedAdapter) {
        synchronized(items) {
            items.sortByDescending { item ->
                item.dateTime?.let { parseDate(it) }
            }
        }
        activity?.runOnUiThread {
            adapter.notifyDataSetChanged()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = binding.rssFeedRecycler
        recyclerView.layoutManager = LinearLayoutManager(context)
        val rssFeedItems = arrayListOf<RssFeedItem>()
        val adapter = RssFeedAdapter(rssFeedItems)
        recyclerView.adapter = adapter
        val seenUrls = mutableSetOf<String>()

        val imageViewToRotate: ImageView = view.findViewById(R.id.LoadingIcon)
        val statusMessage: TextView = view.findViewById(R.id.StatusMessage)

        imageViewToRotate.visibility = View.VISIBLE
        statusMessage.visibility = View.VISIBLE

        startRotationAnimation(imageViewToRotate)

        var completedRssFetches = 0

        for (url in rssFeedStorage.getRssFeedUrls()) {
            if (rssFeedStorage.isRssFeedEnabled(url)) {
                RssFeedFetcher.fetchAndParseRssFeed(url) { rssFeed: RssFeed? ->
                    var iconUrl = rssFeedStorage.getIcon(url)
                    if(iconUrl == null) {
                        RssFeedFetcher.getPageIcon(rssFeed?.channel?.items?.get(0)?.link) {
                            iconUrl = it
                            if (it != null) {
                                rssFeedStorage.setIcon(url, it)
                            }
                        }
                    }
                    val itemsToAdd = mutableListOf<RssFeedItem>()
                    rssFeed?.channel?.items?.forEach { item ->
                        if (seenUrls.add(item.link)) {
                            itemsToAdd.add(
                                RssFeedItem(
                                    item.title,
                                    rssFeed.channel!!.title,
                                    item.description,
                                    item.pubDate,
                                    iconUrl,
                                    item.link
                                )
                            )
                        }
                    }

                    synchronized(rssFeedItems) {
                        rssFeedItems.addAll(itemsToAdd)
                    }
                    sortAndUpdateList(rssFeedItems, adapter)

                    completedRssFetches++

                    if(completedRssFetches == rssFeedStorage.getRssFeedUrls().size) {
                        imageViewToRotate.visibility = View.INVISIBLE
                        statusMessage.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    private fun startRotationAnimation(imageView: ImageView) {
        imageView.animate()
            .rotationBy(360f)
            .withEndAction { startRotationAnimation(imageView) } // Repeat the animation
            .duration = 1000 // Duration of one rotation (in milliseconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}