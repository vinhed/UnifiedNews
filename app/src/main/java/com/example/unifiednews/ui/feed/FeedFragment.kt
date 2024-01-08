package com.example.unifiednews.ui.feed

import android.content.Context
import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.Observer

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private lateinit var rssFeedStorage: RssFeedStorage
    private lateinit var feedViewModel: FeedViewModel

    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        rssFeedStorage = RssFeedStorage(context)
        rssFeedStorage.setOnRssFeedChangeListener(object : RssFeedStorage.OnRssFeedChangeListener {
            override fun onRssFeedChanged() {
                onUserPreferencesChanged()
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        feedViewModel = ViewModelProvider(requireActivity())[FeedViewModel::class.java]
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = binding.rssFeedRecycler
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = RssFeedAdapter(arrayListOf())
        recyclerView.adapter = adapter

        val imageViewToRotate: ImageView = view.findViewById(R.id.LoadingIcon)
        val statusMessage: TextView = view.findViewById(R.id.StatusMessage)

        imageViewToRotate.visibility = View.VISIBLE
        statusMessage.visibility = View.VISIBLE

        feedViewModel.rssFeedItems.observe(viewLifecycleOwner, Observer { items ->
            adapter.updateData(items)
        })

        loadFeedData(imageViewToRotate, statusMessage, adapter)
    }

    private fun loadFeedData(imageViewToRotate: ImageView, statusMessage: TextView, adapter: RssFeedAdapter) {
        var completedRssFetches = 0
        val totalFeeds = rssFeedStorage.getRssFeedUrls().size

        val seenArticleUrls = mutableSetOf<String>()
        for (url in rssFeedStorage.getRssFeedUrls()) {
            if (!feedViewModel.isUrlLoaded(url) && rssFeedStorage.isRssFeedEnabled(url)) {
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
                        if (seenArticleUrls.add(item.link)) {
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
                    feedViewModel.updateRssFeedItems(url, itemsToAdd)

                    updateCompletionStatus(++completedRssFetches, totalFeeds, imageViewToRotate, statusMessage)
                }
            } else {
                updateCompletionStatus(++completedRssFetches, totalFeeds, imageViewToRotate, statusMessage)
                if (!rssFeedStorage.isRssFeedEnabled(url)) {
                    feedViewModel.removeItemsFromUrl(url)
                }
            }
        }
    }
    private fun updateCompletionStatus(completedRssFetches: Int, totalFeeds: Int, imageViewToRotate: ImageView, statusMessage: TextView) {
        if (completedRssFetches == totalFeeds) {
            imageViewToRotate.visibility = View.INVISIBLE
            statusMessage.visibility = View.INVISIBLE
        }
    }

    fun onUserPreferencesChanged() {
        Log.d("RssFrag","onUserPreferencesChanged")
        feedViewModel.refreshFeed()
        val imageViewToRotate: ImageView = view?.findViewById(R.id.LoadingIcon) ?: return
        val statusMessage: TextView = view?.findViewById(R.id.StatusMessage) ?: return
        val adapter = (binding.rssFeedRecycler.adapter as? RssFeedAdapter) ?: return
        loadFeedData(imageViewToRotate, statusMessage, adapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}