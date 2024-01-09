package com.example.unifiednews.repository

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RssFeedStorage(application: Application) {

    private val prefs: SharedPreferences =
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()



    fun setIcon(url: String, iconUrl: String) {
        prefs.edit().putString("ICON$url", iconUrl).apply()
    }

    fun getIcon(url: String): String? {
        return prefs.getString("ICON$url", null)
    }

    fun setRssFeedState(url: String, state: Boolean) {
        prefs.edit().putBoolean(url, state).apply()
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