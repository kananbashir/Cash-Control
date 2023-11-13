package com.example.cashcontrol.data.db.entity.income

import androidx.room.Entity
import com.example.cashcontrol.data.db.entity.Transaction
import com.example.cashcontrol.util.constant.TransactionConstant.TRANSACTION_TYPE_INCOME

@Entity (inheritSuperIndices = true)
data class Income(
    override var transactionAmount: Double,
    override var transactionCurrency: String,
    override var transactionSource: String,
    override var transactionSources: MutableList<String>,
    override var transactionDescription: String,
    override var dateLimitId: Int,
    override var date: String,
    override var dateFrameId: Int
) : Transaction() {
    override var transactionType: String = TRANSACTION_TYPE_INCOME
}