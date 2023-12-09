package com.example.cashcontrol.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.cashcontrol.data.model.NewsResponse
import com.example.cashcontrol.data.network.NetworkResult
import com.example.cashcontrol.data.repository.CashControlRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(private val cashControlRepository: CashControlRepository, application: Application): AndroidViewModel(application) {

    private var _newsResponseResult: MutableLiveData<NetworkResult<NewsResponse>> = MutableLiveData()
    val newsResponseResult: LiveData<NetworkResult<NewsResponse>> get() = _newsResponseResult

    private var _newsSearchResponseResult: MutableLiveData<NetworkResult<NewsResponse>> = MutableLiveData()
    val newsSearchResponseResult: LiveData<NetworkResult<NewsResponse>> get() = _newsSearchResponseResult

    fun getNewsFromApi (page: Int) = viewModelScope.launch {
        apiSafeCall(page)
    }

    fun searchInNews (query: String, page: Int) = viewModelScope.launch {
        apiSafeCall(query, page)
    }

    private suspend fun apiSafeCall (page: Int) {
        _newsResponseResult.value = NetworkResult.Loading()

        if (hasInternetConnection()) {
            try {
                val response = cashControlRepository.newsRemote.getAllNewsFromApi(page)
                _newsResponseResult.value = handleNewsResponse(response)
            } catch (e: Exception) {
                _newsResponseResult.value = NetworkResult.Error("News not found")
            }
        } else {
            _newsResponseResult.value = NetworkResult.Error("No internet connection")
        }

    }

    private suspend fun apiSafeCall (query: String, page: Int) {
        _newsSearchResponseResult.value = NetworkResult.Loading()

        if (hasInternetConnection()) {
            try {
                val response = cashControlRepository.newsRemote.searchInNews(query, page)
                _newsSearchResponseResult.value = handleNewsResponse(response)
            } catch (e: Exception) {
                _newsSearchResponseResult.value = NetworkResult.Error("No result found")
            }
        } else {
            _newsSearchResponseResult.value = NetworkResult.Error("No internet connection")
        }

    }

    private fun handleNewsResponse (newsResponse: Response<NewsResponse>): NetworkResult<NewsResponse> {
        return when (newsResponse.body()!!.status) {
            "error" -> {
                NetworkResult.Error(newsResponse.message())
            }
            "ok" -> {
                NetworkResult.Success(newsResponse.body())
            }
            else -> {
                NetworkResult.Error(newsResponse.message())
            }
        }
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService( Context.CONNECTIVITY_SERVICE ) as ConnectivityManager

        val activeNetwork: Network = connectivityManager.activeNetwork ?: return false
        val capabilities: NetworkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }

}