package com.example.unifiednews.ui.filter

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.unifiednews.R
import com.example.unifiednews.adapters.CustomExpandableListAdapter
import com.example.unifiednews.adapters.RssFilterAdapter
import com.example.unifiednews.databinding.FragmentFilterBinding

import com.example.unifiednews.ui.feed.SharedViewModel

import java.text.ParsePosition


class FilterFragment : Fragment() {

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

    private lateinit var rssFilterAdapter: RssFilterAdapter
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var filterViewModel: FilterViewModel
    private lateinit var expandableListAdapter: CustomExpandableListAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)

        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        filterViewModel = ViewModelProvider(requireActivity())[FilterViewModel::class.java]
        _binding = FragmentFilterBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        _binding!!.createFolder.setOnClickListener {
            _binding!!.folderNameModal.visibility = View.VISIBLE
        }
        _binding!!.cancelName.setOnClickListener {
            _binding!!.folderNameModal.visibility = View.INVISIBLE
            _binding!!.folderName.setText("")
        }
        sharedViewModel.rssFeedChanged.observe(viewLifecycleOwner) {
            if (it) {
                expandableListAdapter.notifyDataSetChanged()
                rssFilterAdapter.notifyDataSetChanged()
            }
        }
        _binding!!.addName.setOnClickListener {
            val folderName: String = _binding!!.folderName.text.toString()
            if (folderName.isBlank()) {
                Toast.makeText(context, "DU ÄR SÄMST", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (filterViewModel.saveFolder(folderName)) {
                Toast.makeText(context, "Folder $folderName created", Toast.LENGTH_SHORT).show()
                val updatedFoldersMap = filterViewModel.getFoldersMap()
                expandableListAdapter.updateFolders(updatedFoldersMap)
                expandableListAdapter.notifyDataSetChanged()
            } else Toast.makeText(context, "Failed to create $folderName ", Toast.LENGTH_SHORT).show()
            _binding!!.folderNameModal.visibility = View.INVISIBLE
            _binding!!.folderName.setText("")
        }
        _binding!!.rssFeedBtn.setOnClickListener {
            val url = _binding!!.rssUrlInput.text.toString()
            if(!filterViewModel.getRssFeedUrls().contains(url)) {
                rssFilterAdapter.isValidRssFeedUrl(url) { isValid ->
                    activity?.runOnUiThread {
                        if (isValid) {
                            Log.d("RssFeed", "Is Valid")
                            _binding!!.rssUrlInput.setText("")
                            filterViewModel.saveRssFeedUrl(url)
                            updateRssFeedList(filterViewModel.getRssFeedUrls())
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
        setupExpandableListView()

        return root
    }
    private fun setupExpandableListView() {
        val expandableListView = binding.root.findViewById<ExpandableListView>(R.id.expandableListView)
        val foldersMap = filterViewModel.getFoldersMap()


        expandableListAdapter = CustomExpandableListAdapter(requireContext(), foldersMap.keys, foldersMap, filterViewModel, sharedViewModel)
        expandableListAdapter.onChildMoreButtonClicked = { url, position, folderName ->
            val context = _binding?.root?.context
            if (context != null) {
                filterViewModel.removeRssFromFolder(folderName, url, position)

            } else {
                Log.e("DO NOT WURK", "WALLA")
            }
        }
        expandableListAdapter.onFolderLongPressed = { folderName, position ->
            showDeleteConfirmationDialog(folderName, position)
        }
        expandableListView.setAdapter(expandableListAdapter)
    }

    private fun showDeleteConfirmationDialog(folderName: String, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Delete Folder")
            .setMessage("Are you sure you want to delete the folder '$folderName'?")
            .setPositiveButton("Delete") { dialog, which ->
                deleteFolder(folderName)

            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteFolder(folderName: String) {

        if (filterViewModel.removeFolder(folderName))Toast.makeText(context, "Success in deleting $folderName", Toast.LENGTH_SHORT).show()
        val updatedFoldersMap = filterViewModel.getFoldersMap()
        Log.d("UPDATEDFOLDERS", updatedFoldersMap.toString())
        expandableListAdapter.updateFolders(updatedFoldersMap)
        expandableListAdapter.notifyDataSetChanged()
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
        rssFilterAdapter = RssFilterAdapter(filterViewModel.getRssFeedUrls(), filterViewModel, { sharedViewModel.notifyRssFeedChanged()}, false, "", sharedViewModel).apply {
            onMoreButtonClicked = { url, position ->
                Log.d("CONTEXT", "CUM")
                val context = _binding?.root?.context
                if (context != null) {
                    val folders = filterViewModel.getFolders()
                    Log.d("FOLDERSMAP", folders.toString())

                    val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, folders)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    _binding?.spinner?.adapter = adapter

                    showMoreModal(url, position)
                }
                onItemRemovedListener = expandableListAdapter

            }

        }
        _binding!!.rssUrls.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = rssFilterAdapter
        }
    }

    private fun showMoreModal(url: String, position: Int) {

        _binding!!.moreModal.visibility = View.VISIBLE
        _binding!!.confirmMore.setOnClickListener {
            if (_binding!!.checkBox2.isChecked) {
                rssFilterAdapter.removeItemAtPosition(position, url)
                _binding!!.checkBox2.isChecked = false

            } else {
                val selectedItem = _binding!!.spinner.selectedItem
                filterViewModel.addUrlToFolder(url, selectedItem.toString())
                val map = filterViewModel.getFoldersMap()
                expandableListAdapter.updateFolders(map)
            }
            _binding!!.moreModal.visibility = View.INVISIBLE
        }
        _binding!!.cancelMore.setOnClickListener {
            _binding!!.moreModal.visibility = View.INVISIBLE
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