package com.example.unifiednews.repository

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.unifiednews.data.RssFeedItem
import com.example.unifiednews.data.RssFilterItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RssFeedStorage(application: Application) {

    private val prefs: SharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()


    private fun storeFilterList(list: List<RssFilterItem>) {
        val jsonString = gson.toJson(list)
        prefs.edit().putString("rssFeedFilterList", jsonString).apply()
    }

    private fun getFilterList(): List<RssFilterItem> {
        val jsonString = prefs.getString("rssFeedFilterList", null)
        return if (jsonString != null) {
            val type = object : TypeToken<List<RssFilterItem>>() {}.type
            gson.fromJson(jsonString, type)
        } else {
            emptyList()
        }
    }

    fun getFilterItem(link: String): RssFilterItem? {
        val currentList = getFilterList().toMutableList()
        return currentList.find { it.link == link }
    }

    fun addFilterItem(item: RssFilterItem) {
        val currentList = getFilterList().toMutableList()
        currentList.add(item)
        storeFilterList(currentList)
    }

    private fun storeBookmarkList(list: List<RssFeedItem>) {
        val jsonString = gson.toJson(list)
        prefs.edit().putString("rssFeedBookmarkList", jsonString).apply()
    }

    fun removeFilterItem(link: String) {
        val currentList = getFilterList().toMutableList()
        currentList.remove(getFilterItem(link))
        storeFilterList(currentList)
    }


    fun getBookmarkList(): List<RssFeedItem> {
        val jsonString = prefs.getString("rssFeedBookmarkList", null)
        return if (jsonString != null) {
            val type = object : TypeToken<List<RssFeedItem>>() {}.type
            gson.fromJson(jsonString, type)
        } else {
            emptyList()
        }
    }

    fun isBookmarked(header: String, dateTime: String): Boolean {
        return getBookmarkList().any { item -> item.header == header && item.dateTime == dateTime }
    }

    fun addBookmarkItem(item: RssFeedItem) {
        val currentList = getBookmarkList().toMutableList()
        currentList.add(item)
        storeBookmarkList(currentList)
    }

    fun removeBookmarkItem(item: RssFeedItem) {
        val currentList = getBookmarkList().toMutableList()
        currentList.remove(item)
        storeBookmarkList(currentList)
    }

    fun setIcon(url: String, iconUrl: String) {
        prefs.edit().putString("ICON$url", iconUrl).apply()
    }

    fun getIcon(url: String): String? {
        return prefs.getString("ICON$url", null)
    }

    fun setRssFeedState(url: String, state: Boolean) {
        prefs.edit().putBoolean(url, state).apply()
    }

    fun getRssFeedState(): Map<String, Boolean> {
        val rssFeedState = mutableMapOf<String, Boolean>()
        val allEntries = prefs.all
        for (entry in allEntries) {
            if (entry.key != RSS_FEED_KEY && entry.key != FOLDERS_KEY) {
                val state = entry.value as? Boolean ?: continue
                rssFeedState[entry.key] = state
            }
        }
        return rssFeedState
    }

    fun isRssFeedEnabled(url: String): Boolean {
        return prefs.getBoolean(url, true)
    }

    fun saveRssFeedUrl(url: String) {
        val existingFeeds = getRssFeedUrls().toMutableSet()
        existingFeeds.add(url)
        prefs.edit().putStringSet(RSS_FEED_KEY, existingFeeds).apply()
    }

    fun addUrlToFolder(url: String, folderName: String): Boolean {
        val foldersMap = getFoldersMap().toMutableMap()

        val urls = foldersMap[folderName]?.toMutableList() ?: mutableListOf()

        if (!urls.contains(url)) {
            urls.add(url)
            foldersMap[folderName] = urls
            saveFoldersMap(foldersMap)
            return true
        }

        return false
    }


    private fun saveFoldersMap(foldersMap: Map<String, List<String>>) {
        val foldersMapString = Gson().toJson(foldersMap)
        prefs.edit().putString(FOLDERS_KEY, foldersMapString).apply()
    }

    fun saveFolderToFolders(folderName: String): Boolean {
        val foldersMap = getFoldersMap().toMutableMap()
        if (!foldersMap.containsKey(folderName)) {
            foldersMap[folderName] = listOf()
            val foldersMapString = gson.toJson(foldersMap)
            prefs.edit().putString(FOLDERS_KEY, foldersMapString).apply()
            return true
        }
        return false
    }

    fun getFolders(): List<String> {
        val foldersMap = getFoldersMap().toMutableMap()
        Log.d("FOLDERSMAP", foldersMap.toString())
        return foldersMap.keys.toList()
    }

    fun getFoldersMap(): Map<String, List<String>> {
        val foldersMapString = prefs.getString(FOLDERS_KEY, null)
        return if (foldersMapString != null) {
            val type = object : TypeToken<Map<String, List<String>>>() {}.type
            gson.fromJson(foldersMapString, type)
        } else {
            emptyMap()
        }
    }

    fun getRssFeedUrls(): List<String> {
        return (prefs.getStringSet(RSS_FEED_KEY, emptySet()) ?: emptySet()).toList()
    }

    fun removeRssFeedUrl(url: String) {
        val feeds = getRssFeedUrls().toMutableSet()
        feeds.remove(url)
        prefs.edit().putStringSet(RSS_FEED_KEY, feeds).apply()
    }

    fun removeRssInFolder(folderName: String, url: String, position: Int): Boolean {
        val foldersMap = getFoldersMap().toMutableMap()
        Log.d("BEFOREREMOVE" , foldersMap.toString())
        foldersMap[folderName]?.let { folderUrls ->
            if (url in folderUrls) {
                val mutableFolderUrls = folderUrls.toMutableList()
                mutableFolderUrls.removeAt(position)

                foldersMap[folderName] = mutableFolderUrls
                saveFoldersMap(foldersMap)
                return true
            }
        }

        return false
    }

    fun removeRssInFolders(url: String) {
        val foldersMap = getFoldersMap().toMutableMap()
        for ((key, urls) in foldersMap) {
            Log.d("urls", urls.toString())
            if (url in urls) {
                val updatedUrls = urls.toMutableList()
                updatedUrls.remove(url)
                foldersMap[key] = updatedUrls
            }
        }
        Log.d(foldersMap.toString(), "FULDER")
        val foldersMapString = gson.toJson(foldersMap)
        prefs.edit().putString(FOLDERS_KEY, foldersMapString).apply()

    }

    companion object {
        private const val PREFS_NAME = "rss_feed_prefs"
        private const val RSS_FEED_KEY = "rss_feed_urls"
        private const val FOLDERS_KEY = "folders_key"
    }

}