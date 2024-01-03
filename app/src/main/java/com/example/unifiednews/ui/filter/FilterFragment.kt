package com.example.unifiednews.ui.filter

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.unifiednews.R
import com.example.unifiednews.adapters.RssFilterAdapter
import com.example.unifiednews.databinding.FragmentFilterBinding
import com.example.unifiednews.repository.RssFeedStorage

class FilterFragment : Fragment() {

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

    private lateinit var rssFilterAdapter: RssFilterAdapter
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
                rssFilterAdapter.isValidRssFeedUrl(url) { isValid ->
                    activity?.runOnUiThread {
                        if (isValid) {
                            Log.d("RssFeed", "Is Valid")
                            _binding!!.rssUrlInput.setText("")
                            rssFeedStorage.saveRssFeedUrl(url)
                            updateRssFeedList(rssFeedStorage.getRssFeedUrls())
                            Toast.makeText(context, "URL saved successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            setInvalidColors()
                        }
                    }
                }
            } else {
                Toast.makeText(context, "Url already saved", Toast.LENGTH_SHORT).show()
                setInvalidColors()
            }
        }

        return root
    }

    private fun setInvalidColors() {
        val colorError = ContextCompat.getColor(requireContext(), R.color.ErrorCode)
        _binding!!.rssInputLayout.apply {
            boxStrokeColor = colorError
            defaultHintTextColor = ColorStateList.valueOf(colorError)
        }
        Toast.makeText(context, "Invalid RSS Feed URL", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({
            val originalColor = ContextCompat.getColor(requireContext(), R.color.TextColorPrimary)
            _binding!!.rssInputLayout.apply {
                boxStrokeColor = originalColor
                defaultHintTextColor = ColorStateList.valueOf(originalColor)
            }
        }, 2000)
    }

    private fun setupRecyclerView() {
        rssFilterAdapter = RssFilterAdapter(rssFeedStorage.getRssFeedUrls())
        _binding!!.rssUrls.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = rssFilterAdapter
        }
    }

    private fun updateRssFeedList(newRssFeedList: List<String>) {
        rssFilterAdapter.apply {
            rssFeedList = newRssFeedList
            notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}