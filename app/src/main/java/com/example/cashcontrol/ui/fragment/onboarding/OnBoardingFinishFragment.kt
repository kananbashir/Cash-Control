package com.example.cashcontrol.ui.fragment.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.cashcontrol.R
import com.example.cashcontrol.data.DateLimitCalculator
import com.example.cashcontrol.data.db.entity.DateFrame
import com.example.cashcontrol.databinding.FragmentOnBoardingFinishBinding
import com.example.cashcontrol.ui.viewmodel.DateFrameViewModel
import com.example.cashcontrol.ui.viewmodel.DateLimitViewModel
import com.example.cashcontrol.ui.viewmodel.ProfileViewModel
import com.example.cashcontrol.ui.viewmodel.TransactionViewModel
import com.example.cashcontrol.ui.viewmodel.UserViewModel
import com.example.cashcontrol.util.DateFrameUtil.checkBudgetAndDateFrameCompatibility
import com.example.cashcontrol.util.constant.DateConstant.DATE_LIMIT_DATE_PATTERN
import com.example.cashcontrol.util.extension.convertDateFromIso8601To
import com.example.cashcontrol.util.extension.getCurrencyCode
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OnBoardingFinishFragment : Fragment() {
    private lateinit var binding: FragmentOnBoardingFinishBinding
    private lateinit var dateFrameViewModel: DateFrameViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var dateLimitViewModel: DateLimitViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var args: OnBoardingFinishFragmentArgs
    private val shrinkButtonAnim: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.shrink_button_anim) }
    private val expandButtonAnim: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.expand_button_anim) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dateFrameViewModel = ViewModelProvider(requireActivity())[DateFrameViewModel::class.java]
        profileViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        dateLimitViewModel = ViewModelProvider(requireActivity())[DateLimitViewModel::class.java]
        transactionViewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]
        args = OnBoardingFinishFragmentArgs.fromBundle(requireArguments())
        binding = FragmentOnBoardingFinishBinding.inflate(layoutInflater)

        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding.apply {

            tvStartPointDateOnBoardingFinishFragOBFinish.text = args.startPointDate.convertDateFromIso8601To(DATE_LIMIT_DATE_PATTERN)
            tvEndPointDateOnBoardingFinishFragOBFinish.text = args.endPointDate.convertDateFromIso8601To(DATE_LIMIT_DATE_PATTERN)
            tvBudgetOnBoardingFinishFragOBFinish.text = "${args.budget} ${args.currency.getCurrencyCode()}"
            tvSavingAmountOnBoardingFinishFragOBFinish.text = "${args.saving} ${args.currency.getCurrencyCode()}"

            btStartTrackingFragOBFinish.setOnClickListener {
                buttonLoadingState(true)
                if (checkBudgetAndDateFrameCompatibility(
                    args.startPointDate,
                    args.endPointDate,
                    (args.budget.toDouble() - args.saving.toDouble())
                )) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        repeatOnLifecycle(Lifecycle.State.STARTED) {
                            userViewModel.getOnlineUser()?.let { onlineUser ->
                                profileViewModel.getOnlineProfileById(onlineUser.userId!!)?.let { onlineProfile ->
                                    if (dateFrameViewModel.getDateFrameOfProfileByDates(args.startPointDate,args.endPointDate,onlineProfile.profileId!!) == null) {
                                        dateFrameViewModel.getOnlineDateFrameByProfile(onlineProfile.profileId!!)?.let { onlineDateFrame ->
                                            dateFrameViewModel.setDateFrameOffline(onlineDateFrame)
                                        }

                                        dateFrameViewModel.upsertDateFrame(DateFrame(
                                            args.startPointDate,
                                            args.endPointDate,
                                            args.budget.toDouble(),
                                            args.currency,
                                            args.saving.toDouble(),
                                            isUnfinished = true,
                                            isOnline = true,
                                            onlineProfile.profileId!!
                                        ))

                                        delay(500) // JUST TO SIMULATE LOADING..
                                        createAllDateLimits(onlineProfile.profileId!!)
                                        findNavController().navigate(OnBoardingFinishFragmentDirections.actionGlobalMainSession2())
                                    } else {
                                        Snackbar.make(
                                            root,
                                            resources.getString(R.string.error_message_onboarding_finish_already_existed_dateframe),
                                            Snackbar.LENGTH_INDEFINITE)
                                            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
                                            .setBackgroundTint(root.resources.getColor(R.color.bittersweet_red, null))
                                            .setAction(resources.getString(R.string.error_message_onboarding_finish_already_existed_dateframe_button)) {
                                                findNavController().popBackStack(R.id.onBoardingDateFrameFragment, false)
                                            }
                                            .show()
                                        buttonLoadingState(false)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Snackbar.make(
                        root,
                        resources.getString(R.string.error_message_onboarding_finish_incompatible_dateframe_and_budget),
                        Snackbar.LENGTH_INDEFINITE)
                        .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
                        .setBackgroundTint(root.resources.getColor(R.color.bittersweet_red, null))
                        .setAction(resources.getString(R.string.error_message_onboarding_finish_already_existed_dateframe_button)) {
                            findNavController().popBackStack(R.id.onBoardingDateFrameFragment, false)
                        }
                        .show()

                    buttonLoadingState(false)
                }
            }

            tvPreviousFragOBFinish.setOnClickListener {
                findNavController().popBackStack()
            }
        }

        return binding.root
    }

    private suspend fun createAllDateLimits(profileId: Int) {
        dateFrameViewModel.getUnfinishedAndOnlineDateFrameByProfile(profileId)?.let { unfinishedDateFrame ->
            val createdDateLimits = DateLimitCalculator()
                .setDateFrame(unfinishedDateFrame)
                .createAllDateLimits()

            dateLimitViewModel.upsertAllDateLimits(*createdDateLimits.toTypedArray())
            transactionViewModel.newExpense = true
            transactionViewModel.newIncome = true
        }
    }

    private fun buttonLoadingState (isLoading: Boolean) {
        binding.apply {
            when (isLoading) {
                true -> {
                    btStartTrackingFragOBFinish.startAnimation(shrinkButtonAnim)
                    ltLoadingFragOBFinish.visibility = View.VISIBLE
                    btStartTrackingFragOBFinish.isClickable = false
                    btStartTrackingFragOBFinish.visibility = View.INVISIBLE
                    tvPreviousFragOBFinish.visibility = View.INVISIBLE
                }
                else -> {
                    btStartTrackingFragOBFinish.startAnimation(expandButtonAnim)
                    ltLoadingFragOBFinish.visibility = View.INVISIBLE
                    btStartTrackingFragOBFinish.isClickable = true
                    btStartTrackingFragOBFinish.visibility = View.VISIBLE
                    tvPreviousFragOBFinish.visibility = View.VISIBLE
                }
            }
        }
    }
}