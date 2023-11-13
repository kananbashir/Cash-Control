package com.example.cashcontrol.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cashcontrol.data.db.entity.DateLimit
import com.example.cashcontrol.data.db.entity.Transaction
import com.example.cashcontrol.data.db.entity.relation.DateLimitWithTransactions
import kotlinx.coroutines.flow.Flow

@Dao
interface DateLimitDao {

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDateLimit (dateLimit: DateLimit)

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAllDateLimit (vararg dateLimit: DateLimit)

    @Delete
    suspend fun deleteDateLimit (dateLimit: DateLimit)

    @Query ("SELECT * FROM date_limit_table")
    fun getAllDateLimitsFromDb (): Flow<List<DateLimit>>

    @androidx.room.Transaction
    @Query ("SELECT * FROM date_limit_table WHERE dateLimitId = :dateLimitId")
    suspend fun getDateLimitWithTransactions (dateLimitId: Int): List<DateLimitWithTransactions>

    @androidx.room.Transaction
    @Query ("SELECT * FROM date_limit_table WHERE dateFrameId = :dateFrameId AND date = :currentDate")
    suspend fun getCurrentDateLimitByDateFrame (dateFrameId: Int, currentDate: String): List<DateLimit>

    @androidx.room.Transaction
    @Query ("SELECT * FROM date_limit_table WHERE dateFrameId = :dateFrameId AND date = :date")
    suspend fun getDateLimitOfDateFrameByDate (dateFrameId: Int, date: String): List<DateLimit>

//    @androidx.room.Transaction
//    @Query ("SELECT * FROM date_limit_table WHERE dateLimitId = :dateLimitId")
//    suspend fun getAllExpensesForDate (dateLimitId: Int): List<Transaction>
//
//    @androidx.room.Transaction
//    @Query ("SELECT * FROM date_limit_table WHERE dateLimitId = :dateLimitId")
//    suspend fun getAllIncomesForDate (dateLimitId: Int): List<Transaction>

}