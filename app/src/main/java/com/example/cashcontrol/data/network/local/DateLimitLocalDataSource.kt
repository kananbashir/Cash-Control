package com.example.cashcontrol.data.network.local

import com.example.cashcontrol.data.db.DateLimitDao
import com.example.cashcontrol.data.db.entity.DateLimit
import com.example.cashcontrol.data.db.entity.Transaction
import com.example.cashcontrol.data.db.entity.relation.DateLimitWithTransactions
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DateLimitLocalDataSource @Inject constructor(private val dateLimitDao: DateLimitDao) {

    suspend fun upsertDateLimit (dateLimit: DateLimit) {
        dateLimitDao.upsertDateLimit(dateLimit)
    }

    suspend fun upsertAllDateLimit (vararg dateLimit: DateLimit) {
        dateLimitDao.upsertAllDateLimit(*dateLimit)
    }

    suspend fun deleteDateLimit (dateLimit: DateLimit) {
        dateLimitDao.deleteDateLimit(dateLimit)
    }

    fun getAllDateLimitsFromDb (): Flow<List<DateLimit>> {
        return dateLimitDao.getAllDateLimitsFromDb()
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

//    suspend fun getAllExpensesForDate (date: String): List<Transaction> {
//        return dateLimitDao.getAllExpensesForDate(date)
//    }
//
//    suspend fun getAllIncomesForDate (date: String): List<Transaction> {
//        return dateLimitDao.getAllIncomesForDate(date)
//    }
}