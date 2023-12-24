package com.example.cashcontrol.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cashcontrol.adapter.listener.TransactionDetailsChildListener
import com.example.cashcontrol.data.TransactionPair
import com.example.cashcontrol.databinding.ItemLayoutTransactionDetailBinding
import com.example.cashcontrol.util.constant.DateConstant
import com.example.cashcontrol.util.extension.convertDateFromIso8601To

class TransactionDetailsParentAdapter: RecyclerView.Adapter<TransactionDetailsParentAdapter.TransactionDetailsParentViewHolder>() {

    private var transactionDetailsChildListener: TransactionDetailsChildListener? = null
    var differ = AsyncListDiffer(this, getDifferCallback())

    inner class TransactionDetailsParentViewHolder (val binding: ItemLayoutTransactionDetailBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionDetailsParentViewHolder {
        return TransactionDetailsParentViewHolder (
            ItemLayoutTransactionDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: TransactionDetailsParentViewHolder, position: Int) {
        val currentItem = differ.currentList[position]

        holder.binding.apply {

            if (position == differ.currentList.size-1) {
                divider.visibility = View.INVISIBLE
            } else {
                divider.visibility = View.VISIBLE
            }

            tvDateItemLayoutTransactionDetail.text = currentItem.dateLimit.date.convertDateFromIso8601To(DateConstant.DATE_LIMIT_DATE_PATTERN)

            val childAdapter = TransactionDetailsChildAdapter()
            childAdapter.setChildListener(transactionDetailsChildListener!!)
            rvItemLayoutTransactionDetail.adapter = childAdapter
            rvItemLayoutTransactionDetail.layoutManager = LinearLayoutManager(holder.binding.root.context)
            childAdapter.differ.submitList(currentItem.transactionList.toList())
        }
    }

    fun setChildListener(listener: TransactionDetailsChildListener) {
        transactionDetailsChildListener = listener
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