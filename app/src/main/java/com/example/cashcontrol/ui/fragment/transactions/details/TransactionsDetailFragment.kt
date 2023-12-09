package com.example.cashcontrol.ui.fragment.transactions.details

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cashcontrol.R
import com.example.cashcontrol.adapter.TransactionDetailsParentAdapter
import com.example.cashcontrol.adapter.TransactionDetailsSearchParentAdapter
import com.example.cashcontrol.adapter.TransactionPair
import com.example.cashcontrol.adapter.listener.TransactionDetailsChildListener
import com.example.cashcontrol.data.db.entity.DateFrame
import com.example.cashcontrol.data.db.entity.Transaction
import com.example.cashcontrol.databinding.FragmentTransactionsDetailBinding
import com.example.cashcontrol.ui.viewmodel.DateFrameViewModel
import com.example.cashcontrol.ui.viewmodel.DateLimitViewModel
import com.example.cashcontrol.ui.viewmodel.ProfileViewModel
import com.example.cashcontrol.ui.viewmodel.TransactionViewModel
import com.example.cashcontrol.ui.viewmodel.UserViewModel
import com.example.cashcontrol.util.MessageUtil
import com.example.cashcontrol.util.constant.TransactionConstant.TRANSACTION_TYPE_EXPENSE
import com.example.cashcontrol.util.constant.UIStateConstant.BUDGET_AMOUNT_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.END_POINT_DATE_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.START_POINT_DATE_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.TOTAL_EXPENSE_AMOUNT_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.TOTAL_INCOME_AMOUNT_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.TOTAL_SAVINGS_AMOUNT_KEY
import com.example.cashcontrol.util.extension.getCurrencySymbol
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TransactionsDetailFragment : Fragment(), TransactionDetailsChildListener {
    private lateinit var binding: FragmentTransactionsDetailBinding
    private lateinit var dateLimitViewModel: DateLimitViewModel
    private lateinit var dateFrameViewModel: DateFrameViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var transactionDetailsParentAdapter: TransactionDetailsParentAdapter
    private lateinit var transactionDetailsSearchParentAdapter: TransactionDetailsSearchParentAdapter
    private var initialLoadingIsFinished: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dateLimitViewModel = ViewModelProvider(requireActivity()).get(DateLimitViewModel::class.java)
        dateFrameViewModel = ViewModelProvider(requireActivity()).get(DateFrameViewModel::class.java)
        transactionViewModel = ViewModelProvider(requireActivity()).get(TransactionViewModel::class.java)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        profileViewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)
        transactionDetailsParentAdapter = TransactionDetailsParentAdapter()
        transactionDetailsSearchParentAdapter = TransactionDetailsSearchParentAdapter()
        transactionDetailsParentAdapter.setChildListener(this)
        binding = FragmentTransactionsDetailBinding.inflate(layoutInflater)

        savedInstanceState?.let {
            binding.apply {
                tvStartPointDateFragTransactionsDetail.text = it.getString(START_POINT_DATE_KEY)
                tvEndPointDateTransactionsDetail.text = it.getString(END_POINT_DATE_KEY)
                tvTotalBudgetFragTransactionsDetail.text = it.getString(BUDGET_AMOUNT_KEY)
                tvTotalExpensesFragTransactionsDetail.text = it.getString(TOTAL_EXPENSE_AMOUNT_KEY)
                tvTotalIncomeFragTransactionsDetail.text = it.getString(TOTAL_INCOME_AMOUNT_KEY)
                tvTotalSavedFragTransactionsDetail.text = it.getString(TOTAL_SAVINGS_AMOUNT_KEY)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding.apply {
            rvFragTransactionsDetail.apply {
                adapter = transactionDetailsParentAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }

            rvSearchFragTransactionsDetail.apply {
                adapter = transactionDetailsSearchParentAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }

            ivReturnBackFragTransactionsDetail.setOnClickListener {
                dateFrameViewModel.clearAllTransactionPairs()
                findNavController().popBackStack()
            }

            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    getUnfinishedDateFrame()?.let { unfinishedDateFrame ->
                        tvStartPointDateFragTransactionsDetail.text = unfinishedDateFrame.startPointDate
                        tvEndPointDateTransactionsDetail.text = unfinishedDateFrame.endPointDate
                        tvTotalBudgetFragTransactionsDetail.text =
                            "${unfinishedDateFrame.totalBudget}${unfinishedDateFrame.mainCurrency.getCurrencySymbol()}"
                        tvTotalExpensesFragTransactionsDetail.text =
                            "${unfinishedDateFrame.totalExpenseOfAll}${unfinishedDateFrame.mainCurrency.getCurrencySymbol()}"
                        tvTotalIncomeFragTransactionsDetail.text =
                            "${unfinishedDateFrame.totalIncomeOfAll}${unfinishedDateFrame.mainCurrency.getCurrencySymbol()}"
                        tvTotalSavedFragTransactionsDetail.text =
                            "${unfinishedDateFrame.savedMoney}${unfinishedDateFrame.mainCurrency.getCurrencySymbol()}"

                        delay(1300)
                        val transactionPairs = dateFrameViewModel.getTransactionPairs(unfinishedDateFrame)
                        transactionDetailsParentAdapter.differ.submitList(transactionPairs.toList())
                        initialLoadingIsFinished = true
                        ltLoadingFragTransactionsDetail.visibility = View.GONE
                        rvFragTransactionsDetail.visibility = View.VISIBLE
                    }
                }
            }

            fabEditFragTransactionsDetail.setOnClickListener {
                    dateFrameViewModel.selectedTransaction?.let {
                    if (it.transactionType == TRANSACTION_TYPE_EXPENSE) {
                        findNavController().navigate(TransactionsDetailFragmentDirections.actionTransactionsDetailFragmentToEditExpenseFragment())
                    } else {
                        findNavController().navigate(TransactionsDetailFragmentDirections.actionTransactionsDetailFragmentToEditIncomeFragment())
                    }
                }
            }

            fabDeleteFragTransactionsDetail.setOnClickListener {
                    dateFrameViewModel.selectedTransaction?.let {
                    val materialAlertDialog = MaterialAlertDialogBuilder(requireContext())
                    materialAlertDialog.setMessage(resources.getString(R.string.alert_message_transactions_detail_delete_transaction))
                        .setPositiveButton(resources.getString(R.string.alert_dialog_positive_yes)) { _, _ ->
                            transactionViewModel.deleteTransaction(it)
                            dateFrameViewModel.clearAllTransactionPairs()
                        }
                        .setNegativeButton(resources.getString(R.string.alert_dialog_negative_no)) { dialogInterface, _ ->
                            dialogInterface.cancel()
                        }
                        .show()
                }
            }

            svSearchFragTransactionsDetail.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    filterList(query!!)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }
            })


            svSearchFragTransactionsDetail.setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (!hasFocus && svSearchFragTransactionsDetail.query.isEmpty()) {
                    binding.rvSearchFragTransactionsDetail.visibility = View.GONE
                    binding.rvFragTransactionsDetail.visibility = View.VISIBLE
                }
            }

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                dateFrameViewModel.allTransactionPairs.collect {
                    getUnfinishedDateFrame()?.let { unfinishedDateFrame ->
                        val transactionPairs = dateFrameViewModel.getTransactionPairs(unfinishedDateFrame)
                        if (transactionPairs.isNotEmpty() && initialLoadingIsFinished) {
                            binding.apply {
                                transactionDetailsParentAdapter.differ.submitList(transactionPairs.toList())

                                if (dateFrameViewModel.selectedTransaction != null) {
                                    fabDeleteFragTransactionsDetail.isClickable = true
                                    fabDeleteFragTransactionsDetail.visibility = View.VISIBLE
                                    fabEditFragTransactionsDetail.isClickable = true
                                    fabEditFragTransactionsDetail.visibility = View.VISIBLE
                                } else {
                                    fabDeleteFragTransactionsDetail.isClickable = false
                                    fabDeleteFragTransactionsDetail.visibility = View.GONE
                                    fabEditFragTransactionsDetail.isClickable = false
                                    fabEditFragTransactionsDetail.visibility = View.GONE
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        binding.apply {
            outState.putString(START_POINT_DATE_KEY, tvStartPointDateFragTransactionsDetail.text.toString())
            outState.putString(END_POINT_DATE_KEY, tvEndPointDateTransactionsDetail.text.toString())
            outState.putString(BUDGET_AMOUNT_KEY, tvTotalBudgetFragTransactionsDetail.text.toString())
            outState.putString(TOTAL_EXPENSE_AMOUNT_KEY, tvTotalExpensesFragTransactionsDetail.text.toString())
            outState.putString(TOTAL_INCOME_AMOUNT_KEY, tvTotalIncomeFragTransactionsDetail.text.toString())
            outState.putString(TOTAL_SAVINGS_AMOUNT_KEY , tvTotalSavedFragTransactionsDetail.text.toString())
        }
    }

    override fun onChildItemSelected(transaction: Transaction) {
        dateFrameViewModel.setSelectionState(transaction)
    }

    private suspend fun getUnfinishedDateFrame(): DateFrame? {
        userViewModel.getOnlineUser()?.let { onlineUser ->
            profileViewModel.getOnlineProfileById(onlineUser.userId!!)?.let { onlineProfile ->
                dateFrameViewModel.getUnfinishedAndOnlineDateFrameByProfile(onlineProfile.profileId!!)?.let { unfinishedDateFrame ->
                    return unfinishedDateFrame
                }
            }
        }
        return null
    }

    private fun filterList(query: String) {
        val filteredList: List<TransactionPair> = dateFrameViewModel.getFilteredList(query)
        if (filteredList.isNotEmpty()) {
            transactionDetailsSearchParentAdapter.differ.submitList(filteredList)
            binding.rvFragTransactionsDetail.visibility = View.GONE
            binding.rvSearchFragTransactionsDetail.visibility = View.VISIBLE
        } else {
            MessageUtil.showNotifyingMessage(resources.getString(R.string.notifying_message_transactions_detail_no_res_found, query), binding)
            binding.rvSearchFragTransactionsDetail.visibility = View.GONE
            binding.rvFragTransactionsDetail.visibility = View.VISIBLE
        }
    }
}