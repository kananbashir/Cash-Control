package com.example.cashcontrol.data.network.local

import com.example.cashcontrol.data.db.DateFrameDao
import com.example.cashcontrol.data.db.entity.DateFrame
import com.example.cashcontrol.data.db.entity.relation.DateFrameWithDateLimits
import com.example.cashcontrol.data.db.entity.relation.DateFrameWithTransactions
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DateFrameLocalDataSource @Inject constructor(private val dateFrameDao: DateFrameDao) {

    suspend fun upsertDateFrame (dateFrame: DateFrame) {
        dateFrameDao.upsertDateFrame(dateFrame)
    }

    suspend fun deleteAllDateFrames (vararg dateFrame: DateFrame) {
        dateFrameDao.deleteAllDateFrames(*dateFrame)
    }

    fun getAllDateFramesFromDb (): Flow<List<DateFrame>> {
        return dateFrameDao.getAllDateFramesFromDb()
    }

    suspend fun getUnfinishedAndOnlineDateFrameByProfile (profileId: Int): List<DateFrame> {
        return dateFrameDao.getUnfinishedAndOnlineDateFrameByProfile(profileId)
    }

    suspend fun getOnlineDateFrameByProfile (profileId: Int): List<DateFrame> {
        return dateFrameDao.getOnlineDateFrameByProfile(profileId)
    }

    suspend fun getDateFrameOfProfileByDates (startPointDate: String, endPointDate: String, profileId: Int): List<DateFrame> {
        return dateFrameDao.getDateFrameOfProfileByDates(startPointDate, endPointDate, profileId)
    }

    suspend fun getDateFrameWithDateLimits (dateFrameId: Int): List<DateFrameWithDateLimits> {
        return dateFrameDao.getDateFrameWithDateLimits(dateFrameId)
    }

    suspend fun getDateFrameWithTransactions (dateFrameId: Int): List<DateFrameWithTransactions> {
        return dateFrameDao.getDateFrameWithTransactions(dateFrameId)
    }

}