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
import com.example.cashcontrol.data.db.entity.DateFrame
import com.example.cashcontrol.data.db.entity.DateLimit
import com.example.cashcontrol.databinding.FragmentOnBoardingFinishBinding
import com.example.cashcontrol.ui.viewmodel.DateFrameViewModel
import com.example.cashcontrol.ui.viewmodel.DateLimitViewModel
import com.example.cashcontrol.ui.viewmodel.ProfileViewModel
import com.example.cashcontrol.ui.viewmodel.TransactionViewModel
import com.example.cashcontrol.ui.viewmodel.UserViewModel
import com.example.cashcontrol.util.constant.DateConstant.DATE_LIMIT_DATE_PATTERN
import com.example.cashcontrol.util.extension.getCurrencySymbol
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.absoluteValue

@AndroidEntryPoint
class OnBoardingFinishFragment : Fragment() {
    private lateinit var binding: FragmentOnBoardingFinishBinding
    private lateinit var dateFrameViewModel: DateFrameViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var dateLimitViewModel: DateLimitViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var args: OnBoardingFinishFragmentArgs
    private val shrinkInsideAnim: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.shrink_inside) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dateFrameViewModel = ViewModelProvider(requireActivity()).get(DateFrameViewModel::class.java)
        profileViewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        dateLimitViewModel = ViewModelProvider(requireActivity()).get(DateLimitViewModel::class.java)
        transactionViewModel = ViewModelProvider(requireActivity()).get(TransactionViewModel::class.java)
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
            tvOnBoardingFinishFragOBFinish.text = resources.getString(
                R.string.text_onboarding_finish_header_1,
                args.budget,
                args.currency.getCurrencySymbol(),
                args.startPointDate,
                args.endPointDate,
                args.saving,
                args.currency.getCurrencySymbol()
            )

            btStartTrackingFragOBFinish.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        userViewModel.getOnlineUser()?.let { onlineUser ->
                            profileViewModel.getOnlineProfileById(onlineUser.userId!!)?.let { onlineProfile ->
                                if (dateFrameViewModel.getDateFrameOfProfileByDates(args.startPointDate,args.endPointDate,onlineProfile.profileId!!) == null) {
                                    updateButtonToLoadingState()

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
                                    delay(200)
                                    createDateLimits(onlineProfile.profileId!!)
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
                                }
                            }
                        }
                    }
                }
            }

            tvPreviousFragOBFinish.setOnClickListener {
                findNavController().popBackStack()
            }
        }

        return binding.root
    }

    private suspend fun createDateLimits (profileId: Int) {
        dateFrameViewModel.getUnfinishedAndOnlineDateFrameByProfile(profileId)?.let { unfinishedDateFrame ->
            val parsedStartPointDate = LocalDate.parse(
                unfinishedDateFrame.startPointDate,
                DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN, Locale.US)
            )
            val daysPassedFromStartPoint = ChronoUnit.DAYS.between(
                parsedStartPointDate,
                LocalDate.now().plusDays(1)
            ).absoluteValue

            val dateLimitList: MutableList<DateLimit> = mutableListOf()

            for (i in 0 until daysPassedFromStartPoint) {
                val updatedDate = parsedStartPointDate.plusDays(i)

                val dateLimit = DateLimit(
                    updatedDate.format(DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN, Locale.US)),
                    unfinishedDateFrame.dateFrameId!!,
                    0.0,
                    0.0
                )
                dateLimitList.add(dateLimit)
            }
            dateLimitViewModel.upsertAllDateLimits(*dateLimitList.toTypedArray())
            transactionViewModel.newExpense = true
            transactionViewModel.newIncome = true
            delay(1300) // JUST TO SIMULATE LOADING..
            findNavController().navigate(OnBoardingFinishFragmentDirections.actionGlobalMainSession2())
        }
    }

    private fun updateButtonToLoadingState() {
        binding.apply {
            btStartTrackingFragOBFinish.startAnimation(shrinkInsideAnim)
            ltLoadingFragOBFinish.visibility = View.VISIBLE
            btStartTrackingFragOBFinish.isClickable = false
            btStartTrackingFragOBFinish.visibility = View.INVISIBLE
            tvPreviousFragOBFinish.visibility = View.INVISIBLE
        }
    }
}