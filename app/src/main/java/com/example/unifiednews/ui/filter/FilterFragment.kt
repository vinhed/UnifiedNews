package com.example.unifiednews.ui.filter

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.unifiednews.adapters.RssFeedAdapter
import com.example.unifiednews.databinding.FragmentFilterBinding
import com.example.unifiednews.repository.RssFeedStorage

class FilterFragment : Fragment() {

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

    private lateinit var rssFeedAdapter: RssFeedAdapter
    private lateinit var rssFeedStorage: RssFeedStorage

    override fun onAttach(context: Context) {
        super.onAttach(context)
        rssFeedStorage = RssFeedStorage(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val filterViewModel = ViewModelProvider(this)[FilterViewModel::class.java]
        _binding = FragmentFilterBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()

        _binding!!.rssFeedBtn.setOnClickListener {
            val url = _binding!!.rssUrlInput.text.toString()
            if(!rssFeedStorage.getRssFeedUrls().contains(url)) {
                rssFeedAdapter.isValidRssFeedUrl(url) { isValid ->
                    activity?.runOnUiThread {
                        if (isValid) {
                            Log.d("RssFeed", "Is Valid")
                            _binding!!.rssUrlInput.setText("")
                            rssFeedStorage.saveRssFeedUrl(url)
                            updateRssFeedList(rssFeedStorage.getRssFeedUrls())
                        } else {
                            Log.d("RssFeed", "Is Not Valid")
                        }
                    }
                }
            } else {
                Log.d("RssFeed", "Url already saved")
            }
        }

        return root
    }

    private fun setupRecyclerView() {
        rssFeedAdapter = RssFeedAdapter(rssFeedStorage.getRssFeedUrls())
        _binding!!.rssUrls.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = rssFeedAdapter
        }
    }

    private fun updateRssFeedList(newRssFeedList: List<String>) {
        rssFeedAdapter.apply {
            rssFeedList = newRssFeedList
            notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}