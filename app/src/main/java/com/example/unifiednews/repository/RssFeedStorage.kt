package com.example.unifiednews.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class RssFeedStorage(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    interface OnRssFeedChangeListener {
        fun onRssFeedChanged()
    }

    private var onRssFeedChangeListener: OnRssFeedChangeListener? = null

    fun setOnRssFeedChangeListener(listener: OnRssFeedChangeListener) {
        this.onRssFeedChangeListener = listener
    }

    private fun notifyRssFeedChanged() {
        onRssFeedChangeListener?.onRssFeedChanged()
    }

    fun setIcon(url: String, iconUrl: String) {
        prefs.edit().putString("ICON$url", iconUrl).apply()
    }

    fun getIcon(url: String): String? {
        return prefs.getString("ICON$url", null)
    }

    fun setRssFeedState(url: String, state: Boolean) {
        prefs.edit().putBoolean(url, state).apply()
        Log.d("RssActive", state.toString())
        notifyRssFeedChanged()
    }

    fun isRssFeedEnabled(url: String): Boolean {
        return prefs.getBoolean(url, true)
    }

    fun saveRssFeedUrl(url: String) {
        val existingFeeds = getRssFeedUrls().toMutableSet()
        existingFeeds.add(url)
        prefs.edit().putStringSet(RSS_FEED_KEY, existingFeeds).apply()
    }

    fun getRssFeedUrls(): List<String> {
        return (prefs.getStringSet(RSS_FEED_KEY, emptySet()) ?: emptySet()).toList()
    }

    fun removeRssFeedUrl(url: String) {
        val feeds = getRssFeedUrls().toMutableSet()
        feeds.remove(url)
        prefs.edit().putStringSet(RSS_FEED_KEY, feeds).apply()
    }

    companion object {
        private const val PREFS_NAME = "rss_feed_prefs"
        private const val RSS_FEED_KEY = "rss_feed_urls"
    }
}