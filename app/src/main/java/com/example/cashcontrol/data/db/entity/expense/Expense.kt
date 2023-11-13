package com.example.cashcontrol.data.db.entity.expense

import androidx.room.Entity
import com.example.cashcontrol.data.db.entity.Transaction
import com.example.cashcontrol.util.constant.TransactionConstant.TRANSACTION_TYPE_EXPENSE

@Entity (inheritSuperIndices = true)
data class Expense(
    override var transactionAmount: Double,
    override var transactionCurrency: String,
    override var transactionCategory: String,
    override var transactionCategories: MutableList<String>,
    override var transactionDescription: String,
    override var dateLimitId: Int,
    override var date: String,
    override var dateFrameId: Int
): Transaction() {
    override var transactionType: String = TRANSACTION_TYPE_EXPENSE
}