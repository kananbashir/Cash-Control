package com.example.cashcontrol.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cashcontrol.data.TransactionPair
import com.example.cashcontrol.databinding.ItemLayoutTransactionDetailSearchBinding
import com.example.cashcontrol.util.constant.DateConstant.DATE_LIMIT_DATE_PATTERN
import com.example.cashcontrol.util.extension.convertDateFromIso8601To

class TransactionDetailsSearchParentAdapter: RecyclerView.Adapter<TransactionDetailsSearchParentAdapter.TransactionDetailsParentViewHolder>() {

    var differ = AsyncListDiffer(this, getDifferCallback())

    inner class TransactionDetailsParentViewHolder (val binding: ItemLayoutTransactionDetailSearchBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionDetailsParentViewHolder {
        return TransactionDetailsParentViewHolder (
            ItemLayoutTransactionDetailSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: TransactionDetailsParentViewHolder, position: Int) {
        val currentItem = differ.currentList[position]

        holder.binding.apply {
            tvDateItemLayoutTransactionDetailSearch.text = currentItem.dateLimit.date.convertDateFromIso8601To(DATE_LIMIT_DATE_PATTERN)

            val childAdapter = TransactionDetailsChildAdapter()
            rvItemLayoutTransactionDetailSearch.adapter = childAdapter
            rvItemLayoutTransactionDetailSearch.layoutManager = LinearLayoutManager(holder.binding.root.context)
            childAdapter.differ.submitList(currentItem.transactionList.toList())
        }
    }

    private fun getDifferCallback(): DiffUtil.ItemCallback<TransactionPair> {
        val differCallback = object : DiffUtil.ItemCallback<TransactionPair>() {
            override fun areItemsTheSame(oldItem: TransactionPair, newItem: TransactionPair): Boolean {
                return oldItem.dateLimit.date == newItem.dateLimit.date
            }

            override fun areContentsTheSame(oldItem: TransactionPair, newItem: TransactionPair): Boolean {
                return oldItem == newItem
            }
        }
        return differCallback
    }
}