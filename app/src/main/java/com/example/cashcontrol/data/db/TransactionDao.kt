package com.example.cashcontrol.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cashcontrol.data.db.entity.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTransaction (transaction: Transaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAllTransactions (vararg transaction: Transaction)

    @Delete
    suspend fun deleteTransaction (transaction: Transaction)

    @Query ("SELECT * FROM transaction_table")
    fun getAllTransactionsFromDb (): Flow<List<Transaction>>

    @androidx.room.Transaction
    @Query ("SELECT * FROM transaction_table WHERE dateFrameId = :dateFrameId")
    suspend fun getAllTransactionsByDateFrame (dateFrameId: Int): List<Transaction>

    @androidx.room.Transaction
    @Query ("SELECT * FROM transaction_table WHERE dateFrameId = :dateFrameId AND transactionId = :transactionId")
    suspend fun getTransactionOfDateFrameById (dateFrameId: Int, transactionId: Int): List<Transaction>

    @androidx.room.Transaction
    @Query ("SELECT * FROM transaction_table WHERE transactionType = :transactionType AND dateFrameId = :dateFrameId")
    suspend fun getAllExpensesByDateFrame (transactionType: String, dateFrameId: Int): List<Transaction>

    @androidx.room.Transaction
    @Query ("SELECT * FROM transaction_table WHERE transactionType = :transactionType AND dateFrameId = :dateFrameId")
    suspend fun getAllIncomesByDateFrame (transactionType: String, dateFrameId: Int): List<Transaction>

}