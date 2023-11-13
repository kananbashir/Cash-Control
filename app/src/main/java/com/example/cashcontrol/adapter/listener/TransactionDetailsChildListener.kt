package com.example.cashcontrol.adapter.listener

import com.example.cashcontrol.data.entity.Transaction

interface TransactionDetailsChildListener {

    fun onChildItemSelected (transaction: Transaction)

}