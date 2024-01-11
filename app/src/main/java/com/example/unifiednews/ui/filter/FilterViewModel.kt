package com.example.unifiednews.ui.filter

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.unifiednews.data.RssFilterItem
import com.example.unifiednews.repository.RssFeedStorage
class FilterViewModel(application: Application) : AndroidViewModel(application) {
    private val rssFeedStorage = RssFeedStorage(application)

    fun saveFolder(folderName: String): Boolean {
        return rssFeedStorage.saveFolderToFolders(folderName)
    }

    fun removeFolder(folderName: String): Boolean {
        return rssFeedStorage.removeFolder(folderName)
    }

    fun addUrlToFolder(url: String, folderName: String): Boolean {
        return rssFeedStorage.addUrlToFolder(url, folderName)
    }

    fun removeRssFromFolder(folderName: String, url: String?, position: Int): Boolean {
        return rssFeedStorage.removeRssInFolder(folderName, url, position)
    }

    fun getFoldersMap(): Map<String, List<String>> {
        return rssFeedStorage.getFoldersMap()
    }
    fun getFolders(): List<String> {
        return rssFeedStorage.getFolders()
    }

    fun getRssFeedUrls(): List<String> {
        return rssFeedStorage.getRssFeedUrls()
    }

    fun saveRssFeedUrl(url: String) {
        rssFeedStorage.saveRssFeedUrl(url)
    }

    fun isRssFeedEnabled(url: String): Boolean {
        return rssFeedStorage.isRssFeedEnabled(url)
    }

    fun getFilterItem(url: String): RssFilterItem? {
        return rssFeedStorage.getFilterItem(url)
    }

    fun addFilterItem(rssFilterItemToAdd: RssFilterItem) {
        rssFeedStorage.addFilterItem(rssFilterItemToAdd)
    }

    fun setRssFeedState(rssFeedUrl: String, checked: Boolean) {
        rssFeedStorage.setRssFeedState(rssFeedUrl, checked)
    }

    fun removeRssFeedUrl(url: String) {
        rssFeedStorage.removeRssFeedUrl(url)
    }

    fun removeRssInFolders(url: String) {
        rssFeedStorage.removeRssInFolders(url)
    }

    fun removeFilterItem(url: String) {
        rssFeedStorage.removeFilterItem(url)
    }

    fun getFolderSwitchBool(folderName: String): Boolean {
        return rssFeedStorage.getFolderSwitchBool(folderName)
    }

    fun setSwitchBoolToFolder(folderName: String, checked: Boolean) {
        rssFeedStorage.setSwitchBoolToFolder(folderName, checked)
    }

}