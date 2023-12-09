package com.example.cashcontrol.data.repository

import com.example.cashcontrol.data.network.local.DateFrameLocalDataSource
import com.example.cashcontrol.data.network.local.DateLimitLocalDataSource
import com.example.cashcontrol.data.network.local.ProfileLocalDataSource
import com.example.cashcontrol.data.network.local.TransactionLocalDataSource
import com.example.cashcontrol.data.network.local.UserLocalDataSource
import com.example.cashcontrol.data.network.remote.NewsRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CashControlRepository @Inject constructor(
    userLocalDataSource: UserLocalDataSource,
    profileLocalDataSource: ProfileLocalDataSource,
    dateFrameLocalDataSource: DateFrameLocalDataSource,
    dateLimitLocalDataSource: DateLimitLocalDataSource,
    transactionLocalDataSource: TransactionLocalDataSource,
    newsRemoteDataSource: NewsRemoteDataSource
) {

    val userLocal = userLocalDataSource
    val profileLocal = profileLocalDataSource
    val dateFrameLocal = dateFrameLocalDataSource
    val dateLimitLocal = dateLimitLocalDataSource
    val transactionsLocal = transactionLocalDataSource
    val newsRemote = newsRemoteDataSource

}