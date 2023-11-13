package com.example.cashcontrol.data.entity.expense

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.cashcontrol.data.entity.Transaction
import com.example.cashcontrol.util.constant.TransactionConstant.TRANSACTION_TYPE_EXPENSE

//@Entity ("expense_table")
//open class Expense(
//    override var transactionAmount: Double = 0.0,
//    override var transactionCurrency: String = "",
//    override var transactionCategory: String = "",
//    override var transactionCategories: MutableList<String> = mutableListOf(),
//    override var transactionDescription: String = "",
//    open var date: String = ""
//) : Transaction() {
//        @PrimaryKey(autoGenerate = true)
//    open var expenseId: Int? = null
//}

@Entity (inheritSuperIndices = true)
open class Expense : Transaction() {
    override var transactionType: String = TRANSACTION_TYPE_EXPENSE
}