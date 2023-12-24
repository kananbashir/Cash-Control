package com.example.cashcontrol.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.cashcontrol.R
import com.example.cashcontrol.data.db.entity.Transaction
import com.example.cashcontrol.databinding.ItemLayoutTransactionsBinding
import com.example.cashcontrol.util.constant.DateConstant.TRANSACTION_DATE_PATTERN
import com.example.cashcontrol.util.constant.TransactionConstant.TRANSACTION_TYPE_EXPENSE
import com.example.cashcontrol.util.constant.TransactionConstant.TRANSACTION_TYPE_INCOME
import com.example.cashcontrol.util.extension.convertDateFromIso8601To
import com.example.cashcontrol.util.extension.getCurrencySymbol

class TransactionAdapter: RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    val differ = AsyncListDiffer(this, getDifferCallBack())

    inner class TransactionViewHolder (val binding: ItemLayoutTransactionsBinding): RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        return TransactionViewHolder (
            ItemLayoutTransactionsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return if (differ.currentList.size > 5) {
            5
        } else {
            differ.currentList.size
        }
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val currentItem = differ.currentList[position]

        holder.binding.apply {
            if (currentItem.transactionType == TRANSACTION_TYPE_EXPENSE) {
                if (currentItem.transactionCategory.isEmpty()) {
                    tvBulkTransactionAmountTransactionsItemLayout.setTextColor(root.resources.getColor(R.color.bittersweet_red, null))
                    cvSingleTransactionTransactionsItemLayout.visibility = View.GONE
                    tvSingleTransactionAmountTransactionsItemLayout.visibility = View.GONE
                    cvBulkTransactionTransactionsItemLayout.visibility = View.VISIBLE
                    tvBulkTransactionAmountTransactionsItemLayout.visibility = View.VISIBLE
                    tvBulkTransactionAmountTransactionsItemLayout.text = "-${currentItem.transactionAmount}${currentItem.transactionCurrency.getCurrencySymbol()}"
                    tvTransactionDateTransactionsItemLayout.text = currentItem.date.convertDateFromIso8601To(TRANSACTION_DATE_PATTERN)
                } else {
                    tvSingleTransactionAmountTransactionsItemLayout.setTextColor(root.resources.getColor(R.color.bittersweet_red, null))
                    cvBulkTransactionTransactionsItemLayout.visibility = View.GONE
                    tvBulkTransactionAmountTransactionsItemLayout.visibility = View.GONE
                    cvSingleTransactionTransactionsItemLayout.visibility = View.VISIBLE
                    tvSingleTransactionAmountTransactionsItemLayout.visibility = View.VISIBLE
                    tvSingleTransactionAmountTransactionsItemLayout.text = "-${currentItem.transactionAmount}${currentItem.transactionCurrency.getCurrencySymbol()}"
                    tvTransactionDateTransactionsItemLayout.text = currentItem.date.convertDateFromIso8601To(TRANSACTION_DATE_PATTERN)
                    tvSingleTransactionCategoryTransactionsItemLayout.text = currentItem.transactionCategory
                }
            } else if (currentItem.transactionType == TRANSACTION_TYPE_INCOME) {
                if (currentItem.transactionSource.isEmpty()) {
                    tvBulkTransactionAmountTransactionsItemLayout.setTextColor(root.resources.getColor(R.color.mantis_green, null))
                    cvSingleTransactionTransactionsItemLayout.visibility = View.GONE
                    tvSingleTransactionAmountTransactionsItemLayout.visibility = View.GONE
                    cvBulkTransactionTransactionsItemLayout.visibility = View.VISIBLE
                    tvBulkTransactionAmountTransactionsItemLayout.visibility = View.VISIBLE
                    tvBulkTransactionAmountTransactionsItemLayout.text = "+${currentItem.transactionAmount}${currentItem.transactionCurrency.getCurrencySymbol()}"
                    tvTransactionDateTransactionsItemLayout.text = currentItem.date.convertDateFromIso8601To(TRANSACTION_DATE_PATTERN)
                } else {
                    tvSingleTransactionAmountTransactionsItemLayout.setTextColor(root.resources.getColor(R.color.mantis_green, null))
                    cvBulkTransactionTransactionsItemLayout.visibility = View.GONE
                    tvBulkTransactionAmountTransactionsItemLayout.visibility = View.GONE
                    cvSingleTransactionTransactionsItemLayout.visibility = View.VISIBLE
                    tvSingleTransactionAmountTransactionsItemLayout.visibility = View.VISIBLE
                    tvSingleTransactionAmountTransactionsItemLayout.text = "+${currentItem.transactionAmount}${currentItem.transactionCurrency.getCurrencySymbol()}"
                    tvTransactionDateTransactionsItemLayout.text = currentItem.date.convertDateFromIso8601To(TRANSACTION_DATE_PATTERN)
                    tvSingleTransactionCategoryTransactionsItemLayout.text = currentItem.transactionSource
                }
            }
        }
    }

    private fun getDifferCallBack(): DiffUtil.ItemCallback<Transaction> {
        val differCallback = object : DiffUtil.ItemCallback<Transaction>() {
            override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
                return oldItem.transactionId == newItem.transactionId
            }

            override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }
        }

        return differCallback
    }
}