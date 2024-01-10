package com.example.unifiednews.managers

object RssFeedStateManager {
    private var checkedStates = mutableMapOf<String, Boolean>()

    fun isRssFeedEnabled(url: String): Boolean = checkedStates.getOrDefault(url, false)

    fun setRssFeedState(url: String, state: Boolean) {
        checkedStates[url] = state
    }
    fun getCheckedStates(): MutableMap<String, Boolean> {
        return checkedStates
    }
}