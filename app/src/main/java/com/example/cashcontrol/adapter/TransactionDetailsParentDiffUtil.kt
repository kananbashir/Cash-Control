package com.example.cashcontrol.adapter

import androidx.recyclerview.widget.DiffUtil
import com.example.cashcontrol.data.entity.DateLimit
import com.example.cashcontrol.data.entity.Transaction

class TransactionDetailsParentDiffUtil(
    private val oldList: List<Pair<DateLimit, List<Transaction>>>,
    private val newList: List<Pair<DateLimit, List<Transaction>>>,
): DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].first.date == newList[newItemPosition].first.date
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}