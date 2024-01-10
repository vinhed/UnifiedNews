package com.example.unifiednews.ui.bookmark

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.unifiednews.R
import com.example.unifiednews.adapters.BookmarkExtendableListAdapter
import com.example.unifiednews.databinding.FragmentBookmarkBinding
import com.example.unifiednews.data.RssFeedItem
import com.example.unifiednews.repository.RssFeedStorage
import com.example.unifiednews.ui.feed.SharedViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class BookmarkFragment : Fragment() {

    private var _binding: FragmentBookmarkBinding? = null
    private lateinit var rssFeedStorage: RssFeedStorage
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        rssFeedStorage = RssFeedStorage(context.applicationContext as Application)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val bookmarkViewModel = ViewModelProvider(this)[BookmarkViewModel::class.java]

        _binding = FragmentBookmarkBinding.inflate(inflater, container, false)
        val root: View = binding.root

        _binding?.closeWebViewButton?.setOnClickListener {
            _binding?.webViewTopBar?.visibility = View.INVISIBLE
            _binding?.webView?.visibility  = View.INVISIBLE
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupExpandableListView()
    }

    private fun setupExpandableListView() {
        val feedItems = rssFeedStorage.getBookmarkList()
        val groupedFeedItems = feedItems.groupBy { it.publisher ?: "Unknown" }
        val publishers = groupedFeedItems.keys.toList()

        val adapter = BookmarkExtendableListAdapter(
            requireContext(),
            publishers,
            groupedFeedItems,
            requireView().findViewById(R.id.webView),
            requireView().findViewById(R.id.webViewTopBar),
            requireView().findViewById(R.id.webViewTopbarText),
            rssFeedStorage
        )

        binding.expandableListView.setAdapter(adapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}