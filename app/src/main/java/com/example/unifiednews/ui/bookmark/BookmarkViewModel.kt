package com.example.unifiednews.ui.bookmark

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.unifiednews.data.RssFeedItem
import com.example.unifiednews.repository.RssFeedStorage

class BookmarkViewModel (application: Application) : AndroidViewModel(application) {
    private val rssFeedStorage = RssFeedStorage(application)


    private val _text = MutableLiveData<String>().apply {
        value = "This is bookmark Fragment"
    }
    val text: LiveData<String> = _text

    fun getBookmarkList():  List<RssFeedItem> {
        return rssFeedStorage.getBookmarkList()
    }
    fun isBookmarked(it1: String, it: String): Boolean {
        return rssFeedStorage.isBookmarked(it1, it)
    }

    fun addBookmarkItem(item: RssFeedItem) {
        return rssFeedStorage.addBookmarkItem(item)
    }

    fun removeBookmarkItem(item: RssFeedItem) {
        rssFeedStorage.removeBookmarkItem(item)
    }
}