package com.example.belensapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.belensapp.api.NewsResponseItem

class HomeViewModel : ViewModel() {
    private val _newsList = MutableLiveData<List<NewsResponseItem>>()
    val newsList: LiveData<List<NewsResponseItem>> get() = _newsList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _showNoData = MutableLiveData<Boolean>()
    val showNoData: LiveData<Boolean> get() = _showNoData

    private var originalNewsList: List<NewsResponseItem> = emptyList()
    private var hasLoadedData = false

    init {
        _isLoading.value = true
        _showNoData.value = false
    }

    fun setNews(news: List<NewsResponseItem>) {
        originalNewsList = news
        _newsList.value = news
        _isLoading.value = false
        _showNoData.value = news.isEmpty()
        hasLoadedData = true
    }

    fun hasLoadedData(): Boolean = hasLoadedData

    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
        // Only hide no data text while loading
        if (isLoading) {
            _showNoData.value = false
        }
    }

    fun filterNews(query: String) {
        if (_isLoading.value == true) return // Don't filter while loading

        val filteredList = if (query.isEmpty()) {
            originalNewsList
        } else {
            originalNewsList.filter { it.title?.contains(query, ignoreCase = true) == true }
        }
        _newsList.value = filteredList
        _showNoData.value = filteredList.isEmpty() && !_isLoading.value!!
    }

    fun shouldRefreshData(): Boolean {
        return !hasLoadedData || originalNewsList.isEmpty()
    }
}