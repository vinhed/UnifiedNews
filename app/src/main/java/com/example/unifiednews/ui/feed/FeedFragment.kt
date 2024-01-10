package com.example.unifiednews.ui.feed

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.unifiednews.R
import com.example.unifiednews.adapters.RssFeedAdapter
import com.example.unifiednews.behavior.CustomBottomSheetBehavior
import com.example.unifiednews.data.RssFeedItem
import com.example.unifiednews.databinding.FragmentFeedBinding
import com.example.unifiednews.repository.RssFeed
import com.example.unifiednews.repository.RssFeedFetcher
import com.example.unifiednews.repository.RssFeedFetcher.fetchAndParseRssFeedAsync
import com.example.unifiednews.repository.RssFeedStorage
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private var adapter: RssFeedAdapter? = null
    private lateinit var rssFeedStorage: RssFeedStorage
    private lateinit var feedViewModel: FeedViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private var coroutineScope = CoroutineScope(Dispatchers.Main)

    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        rssFeedStorage = RssFeedStorage(context.applicationContext as Application)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        feedViewModel = ViewModelProvider(requireActivity())[FeedViewModel::class.java]
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        coroutineScope = CoroutineScope(Dispatchers.Main)
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
            view.findViewById(R.id.bottom_sheet),
            rssFeedStorage
        )
        recyclerView.adapter = adapter
        val bottomSheetBehavior = _binding?.bottomSheet?.let { BottomSheetBehavior.from(it) }
        val behavior = BottomSheetBehavior.from(binding.bottomSheet) as CustomBottomSheetBehavior
        val dragHandle = view.findViewById<View>(R.id.dragHandle)
        behavior.setDraggableHandle(dragHandle)
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior?.peekHeight = 0

        feedViewModel.rssFeedItems.observe(viewLifecycleOwner) { items ->
            coroutineScope.launch {
                adapter!!.updateAndSortData(items)
            }
        }

        sharedViewModel.rssFeedChanged.observe(viewLifecycleOwner) { changed ->
            if (changed) {
                onUserPreferencesChanged()
                sharedViewModel.resetRssFeedChanged()
            }
        }

        _binding?.swipeRefreshLayout?.setOnRefreshListener {
            resetCompletion(true)
        }

        resetCompletion(false)
    }

    private fun resetCompletion(forceReload: Boolean) {
        feedViewModel.resetCompletionStatus()
        _binding?.swipeRefreshLayout?.isRefreshing = true
        if(forceReload) feedViewModel.refreshFeed()
        loadFeedDataWithCoroutines(forceReload)
    }

    private fun loadFeedDataWithCoroutines(forceReload: Boolean) {
        coroutineScope.launch(Dispatchers.IO) {
            val seenArticleUrls = mutableSetOf<String>()
            rssFeedStorage.getRssFeedUrls().forEach { url ->
                if(!seenArticleUrls.contains(url) && rssFeedStorage.isRssFeedEnabled(url) && (!feedViewModel.isUrlLoaded(url) || forceReload)) {
                    val rssFeed = fetchAndParseRssFeedAsync(url)
                    withContext(Dispatchers.Main) {
                        processFetchedRssFeed(rssFeed, url)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        feedViewModel.completionCheck(rssFeedStorage.getRssFeedUrls().size) { updateCompletionStatus() }
                    }
                }
            }
        }
    }

    private fun processFetchedRssFeed(rssFeed: RssFeed?, url: String) {
        var iconUrl = rssFeedStorage.getIcon(url)
        if (iconUrl == null && rssFeed?.channel?.items?.isNotEmpty() == true) {
            RssFeedFetcher.getPageIcon(rssFeed.channel!!.items?.get(0)?.link) {
                iconUrl = it
                if (it != null) {
                    rssFeedStorage.setIcon(url, it)
                }
            }
        }

        val itemsToAdd = rssFeed?.channel?.items?.map { item ->
            RssFeedItem(
                item.title,
                rssFeed.channel!!.title,
                item.description,
                item.pubDate,
                iconUrl,
                item.link
            )
        }.orEmpty()

        feedViewModel.updateRssFeedItems(url, itemsToAdd, rssFeedStorage.getRssFeedUrls().size) { updateCompletionStatus() }
    }

    private fun updateCompletionStatus() {
        val formatter = DateTimeFormatter.ofPattern("dd MMM HH:mm", Locale.ENGLISH)
        rssFeedStorage.setLastUpdate(LocalDateTime.now().format(formatter))
        val statusMessage: TextView? = view?.findViewById(R.id.StatusMessage)
        statusMessage?.text = "Last Update: ${rssFeedStorage.getLastUpdate().toString()}"
        _binding?.swipeRefreshLayout?.isRefreshing = false
    }

    private fun onUserPreferencesChanged() {
        adapter!!.clearData()
        feedViewModel.refreshFeed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        coroutineScope.cancel()
    }
}