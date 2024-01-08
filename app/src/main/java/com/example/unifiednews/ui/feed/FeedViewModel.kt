package com.example.unifiednews.ui.feed

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.unifiednews.data.RssFeedItem

class FeedViewModel : ViewModel() {
    private val _rssFeedItems = MutableLiveData<List<RssFeedItem>>()
    val rssFeedItems: LiveData<List<RssFeedItem>> = _rssFeedItems

    private val loadedUrls = mutableSetOf<String>()

    fun updateRssFeedItems(url: String, newItems: List<RssFeedItem>) {
        loadedUrls.add(url)
        val currentItems = _rssFeedItems.value.orEmpty()
        _rssFeedItems.postValue((currentItems + newItems).toMutableList())
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