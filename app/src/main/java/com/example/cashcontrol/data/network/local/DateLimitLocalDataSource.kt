package com.example.cashcontrol.data.network.local

import com.example.cashcontrol.data.db.DateLimitDao
import com.example.cashcontrol.data.db.entity.DateLimit
import com.example.cashcontrol.data.db.entity.relation.DateLimitWithTransactions
import javax.inject.Inject

class DateLimitLocalDataSource @Inject constructor(private val dateLimitDao: DateLimitDao) {

    suspend fun upsertDateLimit (dateLimit: DateLimit) {
        dateLimitDao.upsertDateLimit(dateLimit)
    }

    suspend fun upsertAllDateLimit (vararg dateLimit: DateLimit) {
        dateLimitDao.upsertAllDateLimit(*dateLimit)
    }

    suspend fun deleteAllDateLimits (vararg dateLimit: DateLimit) {
        dateLimitDao.deleteAllDateLimits(*dateLimit)
    }

    suspend fun getDateLimitWithTransactions(dateLimitId: Int): List<DateLimitWithTransactions> {
        return dateLimitDao.getDateLimitWithTransactions(dateLimitId)
    }

    suspend fun getCurrentDateLimitByDateFrame(dateFrameId: Int, currentDate: String): List<DateLimit> {
        return dateLimitDao.getCurrentDateLimitByDateFrame(dateFrameId, currentDate)
    }

    suspend fun getDateLimitOfDateFrameByDate(dateFrameId: Int, currentDate: String): List<DateLimit> {
        return dateLimitDao.getDateLimitOfDateFrameByDate(dateFrameId, currentDate)
    }
}