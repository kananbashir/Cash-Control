package com.example.cashcontrol.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.cashcontrol.data.db.entity.DateFrame
import com.example.cashcontrol.data.db.entity.relation.DateFrameWithDateLimits
import com.example.cashcontrol.data.db.entity.relation.DateFrameWithTransactions
import kotlinx.coroutines.flow.Flow

@Dao
interface DateFrameDao {

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDateFrame (dateFrame: DateFrame)

    @Delete
    suspend fun deleteDateFrame (dateFrame: DateFrame)

    @Query ("SELECT * FROM dateFrame_table")
    fun getAllDateFramesFromDb (): Flow<List<DateFrame>>

    @Transaction
    @Query ("SELECT * FROM dateFrame_table WHERE profileId = :profileId AND isUnfinished = 1")
    suspend fun getUnfinishedDateFrameByProfile (profileId: Int): List<DateFrame>

    @Transaction
    @Query ("SELECT * FROM dateframe_table WHERE profileId = :profileId AND startPointDate = :startPointDate AND endPointDate = :endPointDate")
    suspend fun getDateFrameOfProfileByDates (startPointDate: String, endPointDate: String, profileId: Int): List<DateFrame>

    @Transaction
    @Query ("SELECT * FROM dateFrame_table WHERE dateFrameId = :dateFrameId")
    suspend fun getDateFrameWithDateLimits (dateFrameId: Int): List<DateFrameWithDateLimits>

    @Transaction
    @Query ("SELECT * FROM dateFrame_table WHERE dateFrameId = :dateFrameId")
    suspend fun getDateFrameWithTransactions (dateFrameId: Int): List<DateFrameWithTransactions>

}