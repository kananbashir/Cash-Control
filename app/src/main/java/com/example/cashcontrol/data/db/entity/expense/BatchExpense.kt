//package com.example.cashcontrol.data.db.entity.expense
//
//import androidx.room.Entity
//
////@Entity("bulk_expense_table")
////data class BulkExpense(
////    override var transactionAmount: Double,
////    override var transactionCurrency: String,
////    override var transactionCategories: MutableList<String>,
////    override var transactionDescription: String,
////    override var date: String
////) : Expense() {
////    @PrimaryKey(autoGenerate = true)
////    override var transactionId: Int? = null
//////    var bulkExpenseId: Int? = null
////}
//
//@Entity(inheritSuperIndices = true)
//data class BatchExpense(
//    override var transactionAmount: Double,
//    override var transactionCurrency: String,
//    override var transactionCategories: MutableList<String>,
//    override var transactionDescription: String,
//    override var date: String,
//    override var dateLimitId: Int,
//    override var dateFrameId: Int
//) : Expense()