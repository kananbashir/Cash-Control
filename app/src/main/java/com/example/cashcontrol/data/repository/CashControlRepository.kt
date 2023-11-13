package com.example.cashcontrol.data.repository

import com.example.cashcontrol.data.network.local.DateFrameLocalDataSource
import com.example.cashcontrol.data.network.local.DateLimitLocalDataSource
import com.example.cashcontrol.data.network.local.ProfileLocalDataSource
import com.example.cashcontrol.data.network.local.TransactionLocalDataSource
import com.example.cashcontrol.data.network.local.UserLocalDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CashControlRepository @Inject constructor(
    userLocalDataSource: UserLocalDataSource,
    profileLocalDataSource: ProfileLocalDataSource,
    dateFrameLocalDataSource: DateFrameLocalDataSource,
    dateLimitLocalDataSource: DateLimitLocalDataSource,
    transactionLocalDataSource: TransactionLocalDataSource,
) {

    val userLocal = userLocalDataSource
    val profileLocal = profileLocalDataSource
    val dateFrameLocal = dateFrameLocalDataSource
    val dateLimitLocal = dateLimitLocalDataSource
    val transactionsLocal = transactionLocalDataSource

}