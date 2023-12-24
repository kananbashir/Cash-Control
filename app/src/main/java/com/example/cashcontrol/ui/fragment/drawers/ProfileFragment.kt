package com.example.cashcontrol.ui.fragment.drawers

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cashcontrol.R
import com.example.cashcontrol.adapter.ProfilesAdapter
import com.example.cashcontrol.adapter.listener.ProfileItemClickListener
import com.example.cashcontrol.data.db.entity.Profile
import com.example.cashcontrol.data.db.entity.User
import com.example.cashcontrol.data.db.entity.relation.UserWithProfiles
import com.example.cashcontrol.databinding.FragmentProfileBinding
import com.example.cashcontrol.ui.viewmodel.DateFrameViewModel
import com.example.cashcontrol.ui.viewmodel.DateLimitViewModel
import com.example.cashcontrol.ui.viewmodel.ProfileViewModel
import com.example.cashcontrol.ui.viewmodel.TransactionViewModel
import com.example.cashcontrol.ui.viewmodel.UserViewModel
import com.example.cashcontrol.util.MessageUtil.showErrorMessage
import com.example.cashcontrol.util.MessageUtil.showNotifyingMessage
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment(), ProfileItemClickListener {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var dateLimitViewModel: DateLimitViewModel
    private lateinit var dateFrameViewModel: DateFrameViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var profilesAdapter: ProfilesAdapter
    private var onlineUser: User? = null
    private var onlineProfile: Profile? = null
    private var onlineUserWithProfiles: UserWithProfiles? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
        dateLimitViewModel = ViewModelProvider(requireActivity())[DateLimitViewModel::class.java]
        dateFrameViewModel = ViewModelProvider(requireActivity())[DateFrameViewModel::class.java]
        transactionViewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        profilesAdapter = ProfilesAdapter()
        profilesAdapter.setListener(this)
        binding = FragmentProfileBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.getOnlineUser()?.let { user ->
                    onlineUser = user
                    profileViewModel.getOnlineProfileById(user.userId!!)?.let { profile ->
                        onlineProfile = profile
                        userViewModel.getUserWithProfiles(user.userId!!)?.let { userWithProfiles ->
                            onlineUserWithProfiles = userWithProfiles
                        }
                    }
                }

                binding.apply {

                    btCreateNewProfileFragProfile.setOnClickListener {
                        findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToOnBoardingProfileFragment())
                    }

                    rvProfilesFragProfile.apply {
                        adapter = profilesAdapter
                        layoutManager = LinearLayoutManager(requireContext())
                    }

                    ivReturnBackFragProfile.setOnClickListener {
                        findNavController().popBackStack()
                    }

                    onlineUserWithProfiles?.let {
                        profilesAdapter.differ.submitList(it.profiles)
                    }

                }
            }
        }

        return binding.root
    }

    override fun onProfileItemClick(profile: Profile, operation: String) {
        val materialAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext())

        when (operation) {
            "update" -> {
                if (onlineProfile != profile) {
                    materialAlertDialogBuilder.setMessage(resources.getString(R.string.alert_message_profile_switch, profile.profileName))
                        .setPositiveButton(resources.getString(R.string.alert_dialog_positive_yes)) { _, _ ->
                            onlineProfile?.let {
                                showNotifyingMessage(resources.getString(R.string.notifying_message_profile_switch, profile.profileName), binding)
                                profileViewModel.setProfileOffline(it)
                                profileViewModel.setProfileOnline(profile)
                                findNavController().popBackStack()
                            }
                        }
                        .setNegativeButton(resources.getString(R.string.alert_dialog_negative_no)) { dialogInterface, _ -> dialogInterface.cancel() }
                        .show()
                }
            }

            else -> {
                materialAlertDialogBuilder.setMessage(resources.getString(R.string.alert_message_profile_delete, profile.profileName))
                    .setPositiveButton(resources.getString(R.string.alert_dialog_positive_yes)) { _, _ ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            repeatOnLifecycle(Lifecycle.State.STARTED) {
                                onlineProfile?.let {
                                    onlineUserWithProfiles?.let { userWithProfiles ->
                                        if (onlineProfile == profile) {
                                            if (userWithProfiles.profiles.size > 1) {
                                                deleteProfile(profile)
                                                showNotifyingMessage(resources.getString(R.string.notifying_message_profile_delete, profile.profileName), binding)
                                                val lastProfile = userViewModel.getUserWithProfiles(onlineUser?.userId!!)?.profiles!!.last()
                                                profileViewModel.setProfileOnline(lastProfile)
                                                onlineProfile = lastProfile
                                            } else {
                                                showErrorMessage(resources.getString(R.string.error_message_profile_delete), binding)
                                            }
                                        } else {
                                            deleteProfile(profile)
                                            showNotifyingMessage(resources.getString(R.string.notifying_message_profile_delete, profile.profileName), binding)
                                        }
                                        delay(200)
                                        profilesAdapter.differ.submitList(userViewModel.getUserWithProfiles(onlineUser?.userId!!)?.profiles)
                                    }
                                }
                            }
                        }
                    }
                    .setNegativeButton(resources.getString(R.string.alert_dialog_negative_no)) { dialogInterface, _ -> dialogInterface.cancel() }
                    .show()
            }
        }
    }

    private suspend fun deleteProfile (profile: Profile) {
        profileViewModel.getProfileWithDateFrames(profile.profileId!!)?.let { profileWithDateFrames ->
            for (dateFrame in profileWithDateFrames.dateFrames) {
                dateFrameViewModel.getDateFrameWithDateLimits(dateFrame.dateFrameId!!)?.let { dateFrameWithDateLimits ->
                    for (dateLimit in dateFrameWithDateLimits.dateLimits) {
                        dateLimitViewModel.getDateLimitWithTransactions(dateLimit.dateLimitId!!)?.let { dateLimitWithTransactions ->
                            transactionViewModel.deleteAllTransactions(*dateLimitWithTransactions.transactions.toTypedArray())
                        }
                    }
                    dateLimitViewModel.deleteAllDateLimits(*dateFrameWithDateLimits.dateLimits.toTypedArray())
                }
            }
            dateFrameViewModel.deleteAllDateFrames(*profileWithDateFrames.dateFrames.toTypedArray())
            profileViewModel.deleteProfile(profile)
        }
    }
}