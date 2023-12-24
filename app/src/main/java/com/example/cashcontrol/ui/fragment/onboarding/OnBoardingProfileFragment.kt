package com.example.cashcontrol.ui.fragment.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.cashcontrol.R
import com.example.cashcontrol.data.db.entity.Profile
import com.example.cashcontrol.databinding.FragmentOnBoardingProfileBinding
import com.example.cashcontrol.ui.viewmodel.ProfileViewModel
import com.example.cashcontrol.ui.viewmodel.UserViewModel
import com.example.cashcontrol.util.MessageUtil.showErrorMessage
import com.example.cashcontrol.util.constant.UIStateConstant.PROFILE_NAME_INPUT_KEY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OnBoardingProfileFragment : Fragment() {
    private lateinit var binding: FragmentOnBoardingProfileBinding
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var userViewModel: UserViewModel
    private val shrinkButtonAnim: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.shrink_button_anim) }
    private val expandButtonAnim: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.expand_button_anim) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel = ViewModelProvider (requireActivity())[UserViewModel::class.java]
        profileViewModel = ViewModelProvider (requireActivity())[ProfileViewModel::class.java]

        binding = FragmentOnBoardingProfileBinding.inflate(layoutInflater)

        if (savedInstanceState != null) {
            binding.etProfileNameFragSignIn.setText(savedInstanceState.getString(PROFILE_NAME_INPUT_KEY))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding.btContinueFragOBProfile.setOnClickListener{
            val profileName = binding.etProfileNameFragSignIn.text.toString()

            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    userViewModel.getOnlineUser()?.let { onlineUser ->
                        if (profileName.isNotEmpty()) {
                            buttonLoadingState(true)
                            delay(500)
                            if (profileViewModel.getProfileOfUserByName(onlineUser.userId!!, profileName) == null) {

                                profileViewModel.getOnlineProfileById(onlineUser.userId!!)?.let { onlineProfile ->
                                    profileViewModel.setProfileOffline(onlineProfile)
                                }

                                profileViewModel.upsertProfile(Profile(profileName, true, onlineUser.userId!!))
                                delay(300)
                                findNavController().navigate(OnBoardingProfileFragmentDirections.actionOnBoardingProfileFragmentToOnboardingSession())
                            } else {
                                showErrorMessage(resources.getString(R.string.error_message_onboarding_profile_used_profile_name), binding)
                                buttonLoadingState(false)
                            }
                        } else {
                            showErrorMessage(resources.getString(R.string.error_message_onboarding_profile_no_profile_name_added), binding)
                        }
                    }
                }
            }
        }

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(PROFILE_NAME_INPUT_KEY, binding.etProfileNameFragSignIn.text.toString())

    }

    private fun buttonLoadingState (isLoading: Boolean) {
        binding.apply {
            when (isLoading) {
                true -> {
                    btContinueFragOBProfile.startAnimation(shrinkButtonAnim)
                    ltLoadingFragOBProfile.visibility = View.VISIBLE
                    btContinueFragOBProfile.isClickable = false
                    btContinueFragOBProfile.visibility = View.INVISIBLE
                }
                else -> {
                    btContinueFragOBProfile.startAnimation(expandButtonAnim)
                    ltLoadingFragOBProfile.visibility = View.INVISIBLE
                    btContinueFragOBProfile.isClickable = true
                    btContinueFragOBProfile.visibility = View.VISIBLE
                }
            }
        }
    }
}