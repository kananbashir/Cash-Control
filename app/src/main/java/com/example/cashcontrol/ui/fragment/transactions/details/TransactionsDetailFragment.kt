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
import com.example.cashcontrol.util.extension.getCurrencySymbol
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
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
//                dateLimitViewModel.clearPairList()
                findNavController().popBackStack()
            }

            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    getUnfinishedDateFrame()?.let { unfinishedDateFrame ->
                        transactionDetailsParentAdapter.differ.submitList(dateFrameViewModel.getTransactionPairs(unfinishedDateFrame))
                        ltLoadingFragTransactionsDetail.visibility = View.GONE
                        rvFragTransactionsDetail.visibility = View.VISIBLE

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
                    }
                }
            }

            fabEditFragTransactionsDetail.setOnClickListener {
                    dateFrameViewModel.selectedTransaction?.let {
                    if (it.transactionType == TRANSACTION_TYPE_EXPENSE) {
                        findNavController().navigate(TransactionsDetailFragmentDirections.actionExpensesDetailFragmentToEditExpenseFragment())
                    } else {
                        findNavController().navigate(TransactionsDetailFragmentDirections.actionExpensesDetailFragmentToEditIncomeFragment())
                    }
                }
            }

            fabDeleteFragTransactionsDetail.setOnClickListener {
                    dateFrameViewModel.selectedTransaction?.let {
                    val materialAlertDialog = MaterialAlertDialogBuilder(requireContext())
                    materialAlertDialog.setMessage("Are you sure you want to delete this transaction?")
                        .setPositiveButton("Yes") { _, _ ->
                            dateFrameViewModel.clearAllTransactionPairs()
                            dateFrameViewModel.setSelectionState(it)
                            transactionViewModel.deleteTransaction(it)
                        }
                        .setNegativeButton("No") { dialogInterface, _ ->
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
                        if (transactionPairs.isNotEmpty()) {
                            binding.apply {
                                transactionDetailsParentAdapter.differ.submitList(transactionPairs.toList())

                                ltLoadingFragTransactionsDetail.visibility = View.GONE
                                rvFragTransactionsDetail.visibility = View.VISIBLE

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

    override fun onChildItemSelected(transaction: Transaction) {
        dateFrameViewModel.setSelectionState(transaction)
    }

    private suspend fun getUnfinishedDateFrame(): DateFrame? {
        userViewModel.getOnlineUser()?.let { onlineUser ->
            profileViewModel.getOnlineProfileById(onlineUser.userId!!)?.let { onlineProfile ->
                dateFrameViewModel.getUnfinishedDateFrameByProfile(onlineProfile.profileId!!)?.let { unfinishedDateFrame ->
                    return unfinishedDateFrame
                }
            }
        }
        return null
    }

    private fun filterList(query: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                getUnfinishedDateFrame()?.let {  unfinishedDateFrame ->
                    val filteredList: List<TransactionPair> = dateFrameViewModel.getFilteredList(query, unfinishedDateFrame)
                    if (filteredList.isNotEmpty()) {
                        transactionDetailsSearchParentAdapter.differ.submitList(filteredList)
                        binding.rvFragTransactionsDetail.visibility = View.INVISIBLE
                        binding.rvSearchFragTransactionsDetail.visibility = View.VISIBLE
                    } else {
                        MessageUtil.showNotifyingMessage("No result found for '$query'", binding)
                        binding.rvSearchFragTransactionsDetail.visibility = View.GONE
                        binding.rvFragTransactionsDetail.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}