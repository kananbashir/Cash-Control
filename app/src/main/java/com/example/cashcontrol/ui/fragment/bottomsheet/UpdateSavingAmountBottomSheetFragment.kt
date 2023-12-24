package com.example.cashcontrol.ui.fragment.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.cashcontrol.R
import com.example.cashcontrol.data.DateLimitCalculator
import com.example.cashcontrol.data.db.entity.DateFrame
import com.example.cashcontrol.databinding.FragmentUpdateSavingAmountBottomSheetBinding
import com.example.cashcontrol.ui.viewmodel.DateFrameViewModel
import com.example.cashcontrol.ui.viewmodel.DateLimitViewModel
import com.example.cashcontrol.ui.viewmodel.ProfileViewModel
import com.example.cashcontrol.ui.viewmodel.TransactionViewModel
import com.example.cashcontrol.ui.viewmodel.UserViewModel
import com.example.cashcontrol.util.DateFrameUtil.checkBudgetAndSubsequentDaysCompatibility
import com.example.cashcontrol.util.MessageUtil.showErrorMessage
import com.example.cashcontrol.util.MessageUtil.showNotifyingMessage
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UpdateSavingAmountBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentUpdateSavingAmountBottomSheetBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var dateFrameViewModel: DateFrameViewModel
    private lateinit var dateLimitViewModel: DateLimitViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private var unfinishedAndOnlineDateFrame: DateFrame? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        profileViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
        dateFrameViewModel = ViewModelProvider(requireActivity())[DateFrameViewModel::class.java]
        dateLimitViewModel = ViewModelProvider(requireActivity())[DateLimitViewModel::class.java]
        transactionViewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]
        binding = FragmentUpdateSavingAmountBottomSheetBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.getOnlineUser()?.let { onlineUser ->
                    profileViewModel.getOnlineProfileById(onlineUser.userId!!)?.let { onlineProfile ->
                        dateFrameViewModel.getUnfinishedAndOnlineDateFrameByProfile(onlineProfile.profileId!!)?.let { unfinishedDateFrame ->
                            unfinishedAndOnlineDateFrame = unfinishedDateFrame
                            binding.etSavingAmountFragUpdateSavingAmount.setText(unfinishedDateFrame.savedMoney.toString())
                        }
                    }
                }
            }
        }

        binding.btConfirmFragUpdateSavingAmount.setOnClickListener {
            val amount = binding.etSavingAmountFragUpdateSavingAmount.text.toString()

            if (amount.isNotEmpty()) {
                unfinishedAndOnlineDateFrame?.let {
                    if (amount.toDouble() != it.savedMoney) {
                        if (amount.toDouble() < (it.getRemainingBudget() + it.savedMoney)) {
                            if (checkBudgetAndSubsequentDaysCompatibility(
                                it.endPointDate,
                                (it.getRemainingBudget() + it.savedMoney) - binding.etSavingAmountFragUpdateSavingAmount.text.toString().toDouble()
                            )) {
                                viewLifecycleOwner.lifecycleScope.launch {
                                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                                        userViewModel.getOnlineUser()?.let { onlineUser ->
                                            profileViewModel.getOnlineProfileById(onlineUser.userId!!)?.let { onlineProfile ->
                                                showNotifyingMessage(resources.getString(R.string.notifying_message_saving_amount_update_success), binding)
                                                dateFrameViewModel.updateSavingAmount(amount.toDouble(), onlineProfile.profileId!!)
                                                delay(300)
                                                dateFrameViewModel.getUnfinishedAndOnlineDateFrameByProfile(onlineProfile.profileId!!)?.let { updatedUnfinishedDateFrame ->
                                                    reCalculateAllExpenseLimits(updatedUnfinishedDateFrame)
                                                    dismiss()
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                showErrorMessage(resources.getString(R.string.error_message_saving_amount_update_incompatible_dateframe_and_budget), binding)
                            }
                        } else {
                            showErrorMessage(resources.getString(R.string.error_message_saving_amount_update_exceeded_amount), binding)
                        }
                    } else {
                        showErrorMessage(resources.getString(R.string.error_message_saving_amount_update_same_amount), binding)
                    }
                }
            } else {
                showErrorMessage(resources.getString(R.string.error_message_saving_amount_update_no_amount), binding)
            }

        }

        return binding.root
    }

    private suspend fun reCalculateAllExpenseLimits(updatedUnfinishedDateFrame: DateFrame) {
        dateFrameViewModel.getDateFrameWithDateLimits(updatedUnfinishedDateFrame.dateFrameId!!)?.let { dateFrameWithDateLimits ->
            dateFrameViewModel.getDateFrameWithTransactions(updatedUnfinishedDateFrame.dateFrameId!!)?.let { dateFrameWithTransactions ->
                val updatedDateLimits = DateLimitCalculator()
                    .setDateFrame(updatedUnfinishedDateFrame)
                    .reCalculateAllExpenseLimits(
                    dateFrameWithDateLimits.dateLimits,
                    dateFrameWithTransactions
                )

                dateLimitViewModel.upsertAllDateLimits(*updatedDateLimits.toTypedArray())
                transactionViewModel.newExpense = true
                transactionViewModel.newIncome = true
            }
        }
    }
}