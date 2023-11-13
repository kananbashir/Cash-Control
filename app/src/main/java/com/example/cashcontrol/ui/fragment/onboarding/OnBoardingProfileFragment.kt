package com.example.cashcontrol.ui.fragment.onboarding

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
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
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OnBoardingProfileFragment : Fragment() {
    private lateinit var binding: FragmentOnBoardingProfileBinding
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var userViewModel: UserViewModel
    private val shrinkInsideAnim: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.shrink_inside) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel = ViewModelProvider (requireActivity()).get(UserViewModel::class.java)
        profileViewModel = ViewModelProvider (requireActivity()).get(ProfileViewModel::class.java)

        binding = FragmentOnBoardingProfileBinding.inflate(layoutInflater)

        if (savedInstanceState != null) {
            binding.etProfileNameFragSignIn.setText(savedInstanceState.getString(PROFILE_NAME_INPUT_KEY))
        }

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

        binding.btContinueFragOBProfile.setOnClickListener{
            val profileName = binding.etProfileNameFragSignIn.text.toString()

            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    userViewModel.getOnlineUser()?.let {
                        if (profileName.isNotEmpty()) {
                            if (profileViewModel.getProfileOfUserByName(it.userId!!, profileName) == null) {
                                updateButtonToLoadingState()
                                profileViewModel.upsertProfile(Profile(profileName, true, it.userId!!))
                                delay(1500) // JUST TO SIMULATE LOADING..
                                findNavController().navigate(OnBoardingProfileFragmentDirections.actionOnBoardingProfileFragmentToOnboardingSession())
                            } else {
                                showErrorMessage("This profile name has already been taken!", binding)
                            }
                        } else {
                            showErrorMessage("Consider adding profile name", binding)
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

    private fun updateButtonToLoadingState () {
        binding.apply {
            btContinueFragOBProfile.startAnimation(shrinkInsideAnim)
            ltLoadingFragOBProfile.visibility = View.VISIBLE
            btContinueFragOBProfile.isClickable = false
            btContinueFragOBProfile.visibility = View.INVISIBLE
        }
    }
}