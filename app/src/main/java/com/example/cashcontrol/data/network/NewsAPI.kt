package com.example.cashcontrol.data.network

import com.example.cashcontrol.data.model.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsAPI {

    @GET ("v2/top-headlines?")
    suspend fun getAllNewsFromApi(
        @Query ("apiKey")
        apiKey: String,
        @Query ("category")
        category: String,
        @Query ("pageSize")
        pageSize: Int,
        @Query ("page")
        page: Int,
    ): Response<NewsResponse>

    @GET ("v2/everything?")
    suspend fun searchInNews (
        @Query ("apiKey")
        apiKey: String,
        @Query ("q")
        query: String,
        @Query ("searchIn")
        searchIn: String,
        @Query ("pageSize")
        pageSize: Int,
        @Query ("page")
        page: Int
    ): Response<NewsResponse>

}