package com.example.cashcontrol.data.network.local

import com.example.cashcontrol.data.db.TransactionDao
import com.example.cashcontrol.data.db.entity.Transaction
import javax.inject.Inject

class TransactionLocalDataSource @Inject constructor(private val transactionDao: TransactionDao) {

    suspend fun upsertTransaction (transaction: Transaction) {
        transactionDao.upsertTransaction(transaction)
    }

    suspend fun deleteTransaction (transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteAllTransactions (vararg transaction: Transaction) {
        transactionDao.deleteAllTransactions(*transaction)
    }

    suspend fun getTransactionOfDateFrameById (dateFrameId: Int, transactionId: Int): List<Transaction> {
        return transactionDao.getTransactionOfDateFrameById(dateFrameId, transactionId)
    }

}