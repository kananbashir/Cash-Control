package com.example.cashcontrol.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.cashcontrol.R
import com.example.cashcontrol.adapter.listener.DateFramesClickListener
import com.example.cashcontrol.data.entity.DateFrame
import com.example.cashcontrol.databinding.ItemLayoutDateFramesBinding
import com.example.cashcontrol.util.extension.getCurrencySymbol

class DateFramesAdapter: RecyclerView.Adapter<DateFramesAdapter.DateFramesViewHolder>() {

    val differ = AsyncListDiffer(this, getDifferCallBack())
    var dateFrameClickListener: DateFramesClickListener? = null

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
            tvDateFrameItemLayoutDateFrames.text = root.resources.getString(R.string.placeholder_text_date_frame, currentItem.startPointDate, currentItem.endPointDate)
            tvTotalBudgetItemLayoutDateFrames.text = "${currentItem.initialBudget}${currentItem.mainCurrency.getCurrencySymbol()}"
            tvTotalExpenseItemLayoutDateFrames.text = "${currentItem.totalExpenseOfAll}${currentItem.mainCurrency.getCurrencySymbol()}"
            tvTotalIncomeItemLayoutDateFrames.text = "${currentItem.totalIncomeOfAll}${currentItem.mainCurrency.getCurrencySymbol()}"

            cvSeeDetailsItemLayoutDateFrames.setOnClickListener {
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