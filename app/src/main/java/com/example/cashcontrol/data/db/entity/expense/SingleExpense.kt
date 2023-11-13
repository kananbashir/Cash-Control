//package com.example.cashcontrol.data.db.entity.expense
//
//import androidx.room.ColumnInfo
//import androidx.room.Entity
//import androidx.room.PrimaryKey
//
////@Entity ("single_expense_table")
////data class SingleExpense(
////    override var transactionAmount: Double,
////    override var transactionCurrency: String,
////    override var transactionCategory: String,
////    override var transactionDescription: String,
////    override var date: String
////) : Expense() {
////    @PrimaryKey(autoGenerate = true)
////    var singleExpenseId: Int? = null
////}
//
//@Entity (inheritSuperIndices = true)
//data class SingleExpense(
//    override var transactionAmount: Double,
//    override var transactionCurrency: String,
//    override var transactionCategory: String,
//    override var transactionDescription: String,
//    override var date: String,
//    override var dateLimitId: Int,
//    override var dateFrameId: Int
//) : Expense()