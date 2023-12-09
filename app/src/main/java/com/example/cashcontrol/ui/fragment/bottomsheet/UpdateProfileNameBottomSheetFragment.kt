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
import com.example.cashcontrol.databinding.FragmentUpdateProfileNameBottomSheetBinding
import com.example.cashcontrol.ui.viewmodel.ProfileViewModel
import com.example.cashcontrol.ui.viewmodel.UserViewModel
import com.example.cashcontrol.util.MessageUtil.showErrorMessage
import com.example.cashcontrol.util.MessageUtil.showNotifyingMessage
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class UpdateProfileNameBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentUpdateProfileNameBottomSheetBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        profileViewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)
        binding = FragmentUpdateProfileNameBottomSheetBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.getOnlineUser()?.let { onlineUser ->
                    profileViewModel.getOnlineProfileById(onlineUser.userId!!)?.let { onlineProfile ->
                        binding.etProfileNameFragUpdateProfileName.setText(onlineProfile.profileName)
                    }
                }
            }
        }

        binding.btConfirmFragUpdateProfileName.setOnClickListener {
            val profileName = binding.etProfileNameFragUpdateProfileName.text.toString()
            if (profileName.isNotEmpty()) {
                viewLifecycleOwner.lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        userViewModel.getOnlineUser()?.let { onlineUser ->
                            profileViewModel.getOnlineProfileById(onlineUser.userId!!)?.let { onlineProfile ->
                                if (profileName != onlineProfile.profileName) {
                                    if (profileViewModel.getProfileOfUserByName(onlineUser.userId!!, profileName) == null) {
                                        profileViewModel.changeOnlineProfileName(onlineProfile, profileName)
                                        showNotifyingMessage(resources.getString(R.string.notifying_message_profile_name_update_success, profileName), binding)
                                        dismiss()
                                    } else {
                                        showErrorMessage(resources.getString(R.string.error_message_profile_name_update_used_profile_name), binding)
                                    }
                                } else {
                                    showErrorMessage(resources.getString(R.string.error_message_profile_name_update_same_profile_name), binding)
                                }
                            }
                        }
                    }
                }
            } else {
                showErrorMessage(resources.getString(R.string.error_message_profile_name_update_no_profile_name), binding)
            }
        }

        return binding.root
    }

}