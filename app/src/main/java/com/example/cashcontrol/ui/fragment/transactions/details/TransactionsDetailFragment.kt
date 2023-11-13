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
import com.example.cashcontrol.data.entity.Transaction
import com.example.cashcontrol.databinding.FragmentTransactionsDetailBinding
import com.example.cashcontrol.ui.viewmodel.DateFrameViewModel
import com.example.cashcontrol.ui.viewmodel.DateLimitViewModel
import com.example.cashcontrol.ui.viewmodel.TransactionViewModel
import com.example.cashcontrol.util.constant.TransactionConstant.TRANSACTION_TYPE_EXPENSE
import com.example.cashcontrol.util.extension.getCurrencySymbol
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TransactionsDetailFragment : Fragment(), TransactionDetailsChildListener {
    private lateinit var binding: FragmentTransactionsDetailBinding
    private lateinit var dateLimitViewModel: DateLimitViewModel
    private lateinit var dateFrameViewModel: DateFrameViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var transactionDetailsParentAdapter: TransactionDetailsParentAdapter
    private lateinit var transactionDetailsSearchParentAdapter: TransactionDetailsSearchParentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dateLimitViewModel = ViewModelProvider(requireActivity()).get(DateLimitViewModel::class.java)
        dateFrameViewModel = ViewModelProvider(requireActivity()).get(DateFrameViewModel::class.java)
        transactionViewModel = ViewModelProvider(requireActivity()).get(TransactionViewModel::class.java)
        transactionDetailsParentAdapter = TransactionDetailsParentAdapter()
        transactionDetailsSearchParentAdapter = TransactionDetailsSearchParentAdapter()
        transactionDetailsParentAdapter.setChildListener(this)

        binding = FragmentTransactionsDetailBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        dateLimitViewModel.startCollectingPairs()

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
                dateLimitViewModel.clearPairList()
                findNavController().popBackStack()
            }

            fabEditFragTransactionsDetail.setOnClickListener {
                val transaction = dateLimitViewModel.selectedTransaction
                transaction?.let {
                    if (it.transactionType == TRANSACTION_TYPE_EXPENSE) {
                        findNavController().navigate(TransactionsDetailFragmentDirections.actionExpensesDetailFragmentToEditExpenseFragment())
                    } else {
                        findNavController().navigate(TransactionsDetailFragmentDirections.actionExpensesDetailFragmentToEditIncomeFragment())
                    }
                }
            }

            fabDeleteFragTransactionsDetail.setOnClickListener {
                val transaction = dateLimitViewModel.selectedTransaction
                transaction?.let {
                    val materialAlertDialog = MaterialAlertDialogBuilder(requireContext())
                    materialAlertDialog.setMessage("Are you sure you want to delete this transaction?")
                        .setPositiveButton("Yes") { _, _ ->
                            dateLimitViewModel.setSelectionState(transaction)
                            transactionViewModel.deleteTransaction(transaction)
                            dateLimitViewModel.startCollectingPairs()
                        }
                        .setNegativeButton("No") { dialogInterface, _ ->
                            dialogInterface.cancel()
                        }
                        .show()
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    dateFrameViewModel.unfinishedDateFrame.collect {
                        it?.let { unfinishedDf ->
                            tvStartPointDateFragTransactionsDetail.text = unfinishedDf.startPointDate
                            tvEndPointDateTransactionsDetail.text = unfinishedDf.endPointDate
                            tvTotalBudgetFragTransactionsDetail.text =
                                "${unfinishedDf.totalBudget}${unfinishedDf.mainCurrency.getCurrencySymbol()}"
                            tvTotalExpensesFragTransactionsDetail.text =
                                "${unfinishedDf.totalExpenseOfAll}${unfinishedDf.mainCurrency.getCurrencySymbol()}"
                            tvTotalIncomeFragTransactionsDetail.text =
                                "${unfinishedDf.totalIncomeOfAll}${unfinishedDf.mainCurrency.getCurrencySymbol()}"
                            tvTotalSavedFragTransactionsDetail.text =
                                "${unfinishedDf.savedMoney}${unfinishedDf.mainCurrency.getCurrencySymbol()}"
                        }
                    }
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

        dateLimitViewModel.allDateLimitsWithTransactions.observe(viewLifecycleOwner) {
            transactionDetailsParentAdapter.differ.submitList(it.toList())
            binding.ltLoadingFragTransactionsDetail.visibility = View.GONE
            binding.rvFragTransactionsDetail.visibility = View.VISIBLE
            binding.apply {
                if (dateLimitViewModel.selectedTransaction != null) {
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

    override fun onChildItemSelected(transaction: Transaction) {
        dateLimitViewModel.setSelectionState(transaction)
    }

    private fun filterList(query: String) {
        val filteredList: List<TransactionPair> = dateLimitViewModel.getFilteredList(query)
        if (filteredList.isNotEmpty()) {
            transactionDetailsSearchParentAdapter.differ.submitList(filteredList)
            binding.rvFragTransactionsDetail.visibility = View.INVISIBLE
            binding.rvSearchFragTransactionsDetail.visibility = View.VISIBLE
        } else {
            showSnackbarMessage("No result found for '$query'")
            binding.rvSearchFragTransactionsDetail.visibility = View.GONE
            binding.rvFragTransactionsDetail.visibility = View.VISIBLE
        }
    }

    private fun showSnackbarMessage (message: String) {
        Snackbar.make(binding.root,message, Snackbar.LENGTH_SHORT)
            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
            .show()
    }
}