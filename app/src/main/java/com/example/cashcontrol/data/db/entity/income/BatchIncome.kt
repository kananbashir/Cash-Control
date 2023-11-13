//package com.example.cashcontrol.data.db.entity.income
//
//import androidx.room.Entity
//
////@Entity ("bulk_income_table")
////data class BulkIncome(
////    override var transactionAmount: Double,
////    override var transactionCurrency: String,
////    override var transactionCategories: MutableList<String>,
////    override var transactionDescription: String,
////    override var date: String
////) : Income() {
////    @PrimaryKey(autoGenerate = true)
////    override var transactionId: Int? = null
//////    var bulkIncomeId: Int? = null
////}
//
//@Entity (inheritSuperIndices = true)
//data class BatchIncome(
//    override var transactionAmount: Double,
//    override var transactionCurrency: String,
//    override var transactionSources: MutableList<String>,
//    override var transactionDescription: String,
//    override var date: String,
//    override var dateLimitId: Int,
//    override var dateFrameId: Int
//) : Income()