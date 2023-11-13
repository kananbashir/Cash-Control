package com.example.cashcontrol.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.cashcontrol.R
import com.example.cashcontrol.adapter.listener.TransactionDetailsChildListener
import com.example.cashcontrol.data.entity.Transaction
import com.example.cashcontrol.databinding.ItemLayoutOfTransactionDetailBinding
import com.example.cashcontrol.util.constant.TransactionConstant.TRANSACTION_TYPE_EXPENSE
import com.example.cashcontrol.util.constant.TransactionConstant.TRANSACTION_TYPE_INCOME
import com.example.cashcontrol.util.extension.concatenateCategories
import com.example.cashcontrol.util.extension.getCurrencySymbol

class TransactionDetailsChildAdapter: RecyclerView.Adapter<TransactionDetailsChildAdapter.TransactionDetailsChildViewHolder>() {

    val differ = AsyncListDiffer(this, getDifferCallback())
    private var transactionDetailsChildListener: TransactionDetailsChildListener? = null

    inner class TransactionDetailsChildViewHolder (val binding: ItemLayoutOfTransactionDetailBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionDetailsChildViewHolder {
        return TransactionDetailsChildViewHolder(
            ItemLayoutOfTransactionDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: TransactionDetailsChildViewHolder, position: Int) {
        val currentItem = differ.currentList[position]

        holder.binding.apply {
            if (currentItem.isSelected) {
                lySelectionItemLayoutOfTransactionDetail.visibility = View.VISIBLE
            } else {
                lySelectionItemLayoutOfTransactionDetail.visibility = View.INVISIBLE
            }

            if (currentItem.transactionType == TRANSACTION_TYPE_EXPENSE) {
                if (currentItem.transactionCategory.isEmpty()) {
                    tvBulkTransactionAmountItemLayoutOfTransactionDetail.setTextColor(root.resources.getColor(R.color.bittersweet_red, null))
                    tvBulkTransactionAmountItemLayoutOfTransactionDetail.text = "-${currentItem.transactionAmount}${currentItem.transactionCurrency.getCurrencySymbol()}"
                    tvBulkCategoriesItemLayoutOfTransactionDetail.text = currentItem.transactionCategories.concatenateCategories()
                    setBulkTransactionItemViews(holder, currentItem)
                } else {
                    tvSingleTransactionAmountItemLayoutOfTransactionDetail.setTextColor(root.resources.getColor(R.color.bittersweet_red, null))
                    tvSingleTransactionAmountItemLayoutOfTransactionDetail.text = "-${currentItem.transactionAmount}${currentItem.transactionCurrency.getCurrencySymbol()}"
                    tvSingleTransactionCategoryItemLayoutOfTransactionDetail.text = currentItem.transactionCategory
                    setSingleTransactionItemViews(holder, currentItem)
                }
            } else if (currentItem.transactionType == TRANSACTION_TYPE_INCOME) {
                if (currentItem.transactionSource.isEmpty()) {
                    tvBulkTransactionAmountItemLayoutOfTransactionDetail.setTextColor(root.resources.getColor(R.color.mantis_green, null))
                    tvBulkTransactionAmountItemLayoutOfTransactionDetail.text = "+${currentItem.transactionAmount}${currentItem.transactionCurrency.getCurrencySymbol()}"
                    tvBulkCategoriesItemLayoutOfTransactionDetail.text = currentItem.transactionSources.concatenateCategories()
                    setBulkTransactionItemViews(holder, currentItem)
                } else {
                    tvSingleTransactionAmountItemLayoutOfTransactionDetail.setTextColor(root.resources.getColor(R.color.mantis_green, null))
                    tvSingleTransactionAmountItemLayoutOfTransactionDetail.text = "+${currentItem.transactionAmount}${currentItem.transactionCurrency.getCurrencySymbol()}"
                    tvSingleTransactionCategoryItemLayoutOfTransactionDetail.text = currentItem.transactionSource
                    setSingleTransactionItemViews(holder, currentItem)
                }
            }

            root.setOnClickListener {
                transactionDetailsChildListener?.onChildItemSelected(currentItem)
            }
        }
    }

    fun setChildListener (listener: TransactionDetailsChildListener) {
        transactionDetailsChildListener = listener
    }

    private fun setSingleTransactionItemViews(holder: TransactionDetailsChildViewHolder, currentItem: Transaction) {
        holder.binding.apply {
            cvBulkTransactionItemLayoutOfTransactionDetail.visibility = View.GONE
            tvBulkTransactionAmountItemLayoutOfTransactionDetail.visibility = View.GONE
            tvBulkCategoriesItemLayoutOfTransactionDetail.visibility = View.GONE
            tvBulkDescriptionItemLayoutOfExpenseDetail.visibility = View.GONE
            cvSingleTransactionItemLayoutOfTransactionDetail.visibility = View.VISIBLE
            tvSingleTransactionAmountItemLayoutOfTransactionDetail.visibility = View.VISIBLE
            if (currentItem.transactionDescription.isNotEmpty()) {
                tvSingleDescriptionItemLayoutOfTransactionDetail.visibility = View.VISIBLE
                tvSingleDescriptionItemLayoutOfTransactionDetail.text = currentItem.transactionDescription
            }
        }
    }

    private fun setBulkTransactionItemViews(holder: TransactionDetailsChildViewHolder, currentItem: Transaction) {
        holder.binding.apply {
            cvSingleTransactionItemLayoutOfTransactionDetail.visibility = View.GONE
            tvSingleTransactionAmountItemLayoutOfTransactionDetail.visibility = View.GONE
            tvSingleDescriptionItemLayoutOfTransactionDetail.visibility = View.GONE
            cvBulkTransactionItemLayoutOfTransactionDetail.visibility = View.VISIBLE
            tvBulkTransactionAmountItemLayoutOfTransactionDetail.visibility = View.VISIBLE
            tvBulkCategoriesItemLayoutOfTransactionDetail.visibility = View.VISIBLE
            if (currentItem.transactionDescription.isNotEmpty()) {
                tvBulkDescriptionItemLayoutOfExpenseDetail.visibility = View.VISIBLE
                tvBulkDescriptionItemLayoutOfExpenseDetail.text = currentItem.transactionDescription
            }
        }
    }

    private fun getDifferCallback(): DiffUtil.ItemCallback<Transaction> {
        val differCallback = object : DiffUtil.ItemCallback<Transaction>() {
            override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
                return oldItem.transactionId == newItem.transactionId
            }

            override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
                return when {
                    oldItem.transactionId != newItem.transactionId -> { false }
                    oldItem.transactionAmount != newItem.transactionAmount -> { false }
                    oldItem.transactionCurrency != newItem.transactionCurrency -> { false }
                    oldItem.transactionCategory != newItem.transactionCategory -> { false }
                    oldItem.transactionCategories != newItem.transactionCategories -> { false }
                    oldItem.transactionSource != newItem.transactionSource -> { false }
                    oldItem.transactionSources != newItem.transactionSources -> { false }
                    oldItem.transactionDescription != newItem.transactionDescription -> { false }
                    oldItem.date != newItem.date -> { false }
                    oldItem.transactionType != newItem.transactionType -> { false }
                    oldItem.isSelected != newItem.isSelected -> { false }
                    else -> {
                        true}
                }
            }
        }

        return differCallback
    }

}