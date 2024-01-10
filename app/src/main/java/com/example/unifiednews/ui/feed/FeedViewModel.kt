package com.example.unifiednews.ui.feed

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.unifiednews.data.RssFeedItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Integer.min
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FeedViewModel : ViewModel() {
    private val _rssFeedItems = MutableLiveData<List<RssFeedItem>>()
    val rssFeedItems: LiveData<List<RssFeedItem>> = _rssFeedItems

    private val loadedUrls = mutableSetOf<String>()
    private var completedRssFetches = 0

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

    fun resetCompletionStatus() {
        completedRssFetches = 0
    }

    fun completionCheck(totalFeeds: Int, completionCallback: () -> Unit) {
        completedRssFetches++
        Log.d("completionCheck", completedRssFetches.toString())
        if(completedRssFetches >= totalFeeds) {
            completionCallback()
        }
    }

    fun updateRssFeedItems(url: String, newItems: List<RssFeedItem>, totalFeeds: Int, completionCallback: () -> Unit) {
        loadedUrls.add(url)
        val currentItems = _rssFeedItems.value.orEmpty().toMutableList()
        val sortedNewItems = newItems.sortedByDescending { it.dateTime?.let { parseDate(it) } }
        val finalList = mutableListOf<RssFeedItem>()

        var currentIndex = 0
        var newIndex = 0

        while (currentIndex < currentItems.size && newIndex < sortedNewItems.size) {
            val current = currentItems[currentIndex]
            val new = sortedNewItems[newIndex]
            val currentDateTime = current.dateTime?.let { parseDate(it) }
            val newDateTime = new.dateTime?.let { parseDate(it) }

            if (newDateTime != null && (currentDateTime == null || newDateTime > currentDateTime)) {
                finalList.add(new)
                newIndex++
            } else {
                finalList.add(current)
                currentIndex++
            }
        }

        finalList.addAll(currentItems.subList(currentIndex, currentItems.size))
        finalList.addAll(sortedNewItems.subList(newIndex, sortedNewItems.size))

        _rssFeedItems.postValue(finalList)

        completionCheck(totalFeeds, completionCallback)
    }

    fun removeItemsFromUrl(url: String) {
        val currentItems = _rssFeedItems.value.orEmpty()
        _rssFeedItems.postValue(currentItems.filterNot { it.link == url }.toMutableList())
        loadedUrls.remove(url)
    }

    fun isUrlLoaded(url: String): Boolean = loadedUrls.contains(url)

    fun refreshFeed() {
        loadedUrls.clear()
        _rssFeedItems.value = emptyList()
    }
}