package com.example.unifiednews.ui.feed

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _rssFeedChanged = MutableLiveData<Boolean>()

    val rssFeedChanged: LiveData<Boolean> = _rssFeedChanged

    fun notifyRssFeedChanged() {
        _rssFeedChanged.value = true
    }

    fun resetRssFeedChanged() {
        _rssFeedChanged.value = false
    }
}