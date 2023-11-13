package com.example.cashcontrol.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cashcontrol.data.entity.Transaction
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
    @Query ("SELECT * FROM transaction_table WHERE transactionType = :transactionType")
    suspend fun getAllExpenses (transactionType: String): List<Transaction>

    @androidx.room.Transaction
    @Query ("SELECT * FROM transaction_table WHERE transactionType = :transactionType")
    suspend fun getAllIncomes (transactionType: String): List<Transaction>

}