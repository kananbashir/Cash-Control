package com.example.cashcontrol.data.network.local

import com.example.cashcontrol.data.db.DateFrameDao
import com.example.cashcontrol.data.entity.DateFrame
import com.example.cashcontrol.data.entity.relation.DateFrameWithDateLimits
import com.example.cashcontrol.data.entity.relation.DateFrameWithTransactions
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DateFrameLocalDataSource @Inject constructor(private val dateFrameDao: DateFrameDao) {

    suspend fun upsertDateFrame (dateFrame: DateFrame) {
        dateFrameDao.upsertDateFrame(dateFrame)
    }

    suspend fun deleteDateFrame (dateFrame: DateFrame) {
        dateFrameDao.deleteDateFrame(dateFrame)
    }

    fun getAllDateFramesFromDb (): Flow<List<DateFrame>> {
        return dateFrameDao.getAllDateFramesFromDb()
    }

    suspend fun getUnfinishedDateFrame (): List<DateFrame> {
        return dateFrameDao.getUnfinishedDateFrame()
    }

    suspend fun getDateFrameWithDateLimits (dateFrameId: Int): List<DateFrameWithDateLimits> {
        return dateFrameDao.getDateFrameWithDateLimits(dateFrameId)
    }

    suspend fun getDateFrameWithTransactions (dateFrameId: Int): List<DateFrameWithTransactions> {
        return dateFrameDao.getDateFrameWithTransactions(dateFrameId)
    }

}