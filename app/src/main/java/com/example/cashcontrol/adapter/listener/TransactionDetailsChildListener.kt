package com.example.cashcontrol.adapter.listener

import com.example.cashcontrol.data.db.entity.Transaction

interface TransactionDetailsChildListener {

    fun onChildItemSelected (transaction: Transaction)

}