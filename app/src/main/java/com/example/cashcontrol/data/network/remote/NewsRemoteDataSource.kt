package com.example.cashcontrol.data.network.remote

import com.example.cashcontrol.data.model.NewsResponse
import com.example.cashcontrol.data.network.NewsAPI
import com.example.cashcontrol.util.constant.NetworkConstant.NEWS_API_CATEGORY
import com.example.cashcontrol.util.constant.NetworkConstant.NEWS_API_KEY
import com.example.cashcontrol.util.constant.NetworkConstant.NEWS_API_PAGE_SIZE
import com.example.cashcontrol.util.constant.NetworkConstant.NEWS_API_SEARCH_IN_FIELD
import retrofit2.Response
import javax.inject.Inject

class NewsRemoteDataSource @Inject constructor(private val newsAPI: NewsAPI) {

    suspend fun getAllNewsFromApi (page: Int): Response<NewsResponse> {
        return newsAPI.getAllNewsFromApi(
            NEWS_API_KEY,
            NEWS_API_CATEGORY,
            NEWS_API_PAGE_SIZE,
            page
        )
    }

    suspend fun searchInNews (query: String, page: Int): Response<NewsResponse> {
        return newsAPI.searchInNews(
            NEWS_API_KEY,
            query,
            NEWS_API_SEARCH_IN_FIELD,
            NEWS_API_PAGE_SIZE,
            page
        )
    }

}