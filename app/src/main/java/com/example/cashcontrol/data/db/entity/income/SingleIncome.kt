//package com.example.cashcontrol.data.db.entity.income
//
//import androidx.room.ColumnInfo
//import androidx.room.Embedded
//import androidx.room.Entity
//import androidx.room.PrimaryKey
//import com.example.cashcontrol.data.db.entity.expense.Expense
//
////@Entity ("single_income_table")
////data class SingleIncome(
////    override var transactionAmount: Double,
////    override var transactionCurrency: String,
////    override var transactionCategory: String,
////    override var transactionDescription: String,
////    override var date: String
////) : Income() {
////    @PrimaryKey(autoGenerate = true)
////    override var transactionId: Int? = null
//////    var singleIncomeId: Int? = null
////}
//
//@Entity (inheritSuperIndices = true)
//data class SingleIncome(
//    override var transactionAmount: Double,
//    override var transactionCurrency: String,
//    override var transactionSource: String,
//    override var transactionDescription: String,
//    override var date: String,
//    override var dateLimitId: Int,
//    override var dateFrameId: Int
//) : Income()
