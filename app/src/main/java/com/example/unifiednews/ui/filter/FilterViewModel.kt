package com.example.unifiednews.ui.filter

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.unifiednews.repository.RssFeedStorage
class FilterViewModel(application: Application) : AndroidViewModel(application) {
    private val rssFeedStorage = RssFeedStorage(application)
    fun saveFolder(folderName: String):Boolean {
        Log.d(folderName, "folderName")

        return rssFeedStorage.saveFolderToFolders(folderName)
    }

    fun addToFolder(url: String, folderName: String):Boolean {
        return rssFeedStorage.addUrlToFolder(url, folderName)
    }


}