package com.example.unifiednews.ui.feed

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private var adapter: RssFeedAdapter? = null
    private lateinit var rssFeedStorage: RssFeedStorage
    private lateinit var feedViewModel: FeedViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        rssFeedStorage = RssFeedStorage(context.applicationContext as Application)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        feedViewModel = ViewModelProvider(requireActivity())[FeedViewModel::class.java]
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = binding.rssFeedRecycler
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = RssFeedAdapter(
            arrayListOf(),
            view.findViewById(R.id.webView),
            view.findViewById(R.id.webViewTopBar),
            view.findViewById(R.id.webViewTopbarText),
            rssFeedStorage
        )
        recyclerView.adapter = adapter

        val imageViewToRotate: ImageView = view.findViewById(R.id.LoadingIcon)
        val statusMessage: TextView = view.findViewById(R.id.StatusMessage)

        imageViewToRotate.visibility = View.VISIBLE
        statusMessage.visibility = View.VISIBLE

        _binding?.closeWebViewButton?.setOnClickListener {
            _binding?.webViewTopBar?.visibility = View.INVISIBLE
            _binding?.webView?.visibility  = View.INVISIBLE
        }

        feedViewModel.rssFeedItems.observe(viewLifecycleOwner) { items ->
            adapter!!.updateData(items)
        }

        sharedViewModel.rssFeedChanged.observe(viewLifecycleOwner) { changed ->
            if (changed) {
                onUserPreferencesChanged()
                sharedViewModel.resetRssFeedChanged()
            }
        }

        startRotationAnimation(imageViewToRotate)
        loadFeedDataWithCoroutines()
    }

    private fun loadFeedDataWithCoroutines() {
        Log.d("RssFeed", "Outside")

        var completedRssFetches = 0
        val totalFeeds = rssFeedStorage.getRssFeedUrls().size

        coroutineScope.launch {
            Log.d("RssFeed", "Inside")
            val seenArticleUrls = mutableSetOf<String>()
            rssFeedStorage.getRssFeedUrls().forEach { url ->
                if (rssFeedStorage.isRssFeedEnabled(url) && !feedViewModel.isUrlLoaded(url)) {
                    withContext(Dispatchers.IO) {
                        RssFeedFetcher.fetchAndParseRssFeed(url) { rssFeed ->
                            processFetchedRssFeed(rssFeed, url, seenArticleUrls)
                        }
                    }
                }
                completedRssFetches++
                Log.d("RssFeedFetched", completedRssFetches.toString())
            }
            updateCompletionStatus(completedRssFetches, totalFeeds)
        }
        updateCompletionStatus(completedRssFetches, 0)
    }

    private fun processFetchedRssFeed(rssFeed: RssFeed?, url: String, seenArticleUrls: MutableSet<String>) {
        var iconUrl = rssFeedStorage.getIcon(url)
        if (iconUrl == null && rssFeed?.channel?.items?.isNotEmpty() == true) {
            RssFeedFetcher.getPageIcon(rssFeed.channel!!.items?.get(0)?.link) {
                iconUrl = it
                if (it != null) {
                    rssFeedStorage.setIcon(url, it)
                }
            }
        }

        val itemsToAdd = rssFeed?.channel?.items?.mapNotNull { item ->
            if (seenArticleUrls.add(item.link)) {
                RssFeedItem(
                    item.title,
                    rssFeed.channel!!.title,
                    item.description,
                    item.pubDate,
                    iconUrl,
                    item.link
                )
            } else null
        }.orEmpty()

        feedViewModel.updateRssFeedItems(url, itemsToAdd)
    }

    /*private fun loadFeedData(imageViewToRotate: ImageView, statusMessage: TextView) {
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
    }*/

    private fun startRotationAnimation(imageView: ImageView) {
        imageView.animate()
            .rotationBy(360f)
            .withEndAction { startRotationAnimation(imageView) }
            .duration = 1000
    }

    private fun updateCompletionStatus(completedRssFetches: Int, totalFeeds: Int) {
        Log.d("updateCompletionStatus", completedRssFetches.toString() + " " + totalFeeds.toString());
        if (completedRssFetches >= totalFeeds) {
            val imageViewToRotate: ImageView? = view?.findViewById(R.id.LoadingIcon)
            val statusMessage: TextView? = view?.findViewById(R.id.StatusMessage)
            imageViewToRotate?.visibility = View.INVISIBLE
            statusMessage?.visibility = View.INVISIBLE
        }
    }

    private fun onUserPreferencesChanged() {
        Log.d("RssFeed", "Refresh Feed - Load new values")
        adapter!!.clearData()
        feedViewModel.refreshFeed()
        Log.d("RssFeed", "Here")
        //loadFeedDataWithCoroutines()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        coroutineScope.cancel()
    }
}