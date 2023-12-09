package com.example.cashcontrol.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.cashcontrol.R
import com.example.cashcontrol.adapter.listener.DateFramesClickListener
import com.example.cashcontrol.data.db.entity.DateFrame
import com.example.cashcontrol.databinding.ItemLayoutDateFramesBinding
import com.example.cashcontrol.util.extension.getCurrencySymbol

class DateFramesAdapter: RecyclerView.Adapter<DateFramesAdapter.DateFramesViewHolder>() {

    val differ = AsyncListDiffer(this, getDifferCallBack())
    private var dateFrameClickListener: DateFramesClickListener? = null

    inner class DateFramesViewHolder (val binding: ItemLayoutDateFramesBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateFramesViewHolder {
        return DateFramesViewHolder (
            ItemLayoutDateFramesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: DateFramesViewHolder, position: Int) {
        val currentItem = differ.currentList[position]

        holder.binding.apply {
            val mainCurrencySymbol = currentItem.mainCurrency.getCurrencySymbol()
            tvDateFrameItemLayoutDateFrames.text = root.resources.getString(R.string.placeholder_text_date_frame, currentItem.startPointDate, currentItem.endPointDate)
            tvTotalBudgetItemLayoutDateFrames.text = "${currentItem.initialBudget}$mainCurrencySymbol"
            tvTotalExpenseItemLayoutDateFrames.text = "${currentItem.totalExpenseOfAll}$mainCurrencySymbol"
            tvTotalIncomeItemLayoutDateFrames.text = "${currentItem.totalIncomeOfAll}$mainCurrencySymbol"
            tvTotalSavedItemLayoutDateFrames.text = "${currentItem.savedMoney} $mainCurrencySymbol"

            if (position == differ.currentList.size-1) {
                divider.visibility = View.INVISIBLE
            } else {
                divider.visibility = View.VISIBLE
            }

            when {
                (!currentItem.isUnfinished && !currentItem.isOnline) -> {
                    cvOpenItemLayoutDateFrames.visibility = View.GONE
                    tvFinishedItemLayoutDateFrames.visibility = View.VISIBLE
                }
                (currentItem.isUnfinished && !currentItem.isOnline) -> {
                    cvOpenItemLayoutDateFrames.visibility = View.VISIBLE
                    tvFinishedItemLayoutDateFrames.visibility = View.GONE
                }
                else -> {
                    cvOpenItemLayoutDateFrames.visibility = View.GONE
                    tvFinishedItemLayoutDateFrames.visibility = View.GONE
                }
            }

            cvOpenItemLayoutDateFrames.setOnClickListener {
                dateFrameClickListener?.onDateFrameClicked(currentItem)
            }
        }
    }

    private fun getDifferCallBack(): DiffUtil.ItemCallback<DateFrame> {
        val differCallBack = object : DiffUtil.ItemCallback<DateFrame>() {
            override fun areItemsTheSame(oldItem: DateFrame, newItem: DateFrame): Boolean {
                return oldItem.dateFrameId == newItem.dateFrameId
            }

            override fun areContentsTheSame(oldItem: DateFrame, newItem: DateFrame): Boolean {
                return oldItem == newItem
            }
        }

        return differCallBack
    }

    fun setListener (listener: DateFramesClickListener) {
        dateFrameClickListener = listener
    }
}