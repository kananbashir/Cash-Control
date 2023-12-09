package com.example.cashcontrol.ui.fragment.login

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
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
import com.example.cashcontrol.databinding.FragmentSigninBinding
import com.example.cashcontrol.ui.viewmodel.DateFrameViewModel
import com.example.cashcontrol.ui.viewmodel.ProfileViewModel
import com.example.cashcontrol.ui.viewmodel.UserViewModel
import com.example.cashcontrol.util.MessageUtil.showErrorMessage
import com.example.cashcontrol.util.constant.UIStateConstant.PASSWORD_INPUT_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.USERNAME_INPUT_KEY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SigninFragment : Fragment() {
    private lateinit var binding: FragmentSigninBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var dateFrameViewModel: DateFrameViewModel
    private val shrinkInsideAnim: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.shrink_inside) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel = ViewModelProvider (requireActivity()).get(UserViewModel::class.java)
        profileViewModel = ViewModelProvider (requireActivity()).get(ProfileViewModel::class.java)
        dateFrameViewModel = ViewModelProvider (requireActivity()).get(DateFrameViewModel::class.java)

        binding = FragmentSigninBinding.inflate(layoutInflater)

        if (savedInstanceState != null) {
            binding.apply {
                etUsernameFragSignIn.setText(savedInstanceState.getString(USERNAME_INPUT_KEY))
                etPasswordFragSignIn.setText(savedInstanceState.getString(PASSWORD_INPUT_KEY))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding.apply {
            btSignInFragSignIn.setOnClickListener {
                val username = binding.etUsernameFragSignIn.text.toString()
                val password = binding.etPasswordFragSignIn.text.toString()


                if (username.isNotEmpty() && password.isNotEmpty()) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        repeatOnLifecycle(Lifecycle.State.STARTED) {
                            val foundUser = userViewModel.getUserByNameAndPassword(username, password)
                            if (foundUser != null) {
                                    val onlineProfile = profileViewModel.getOnlineProfileById(foundUser.userId!!)
                                    if (onlineProfile != null) {
                                        val unfinishedDateFrame = dateFrameViewModel.getUnfinishedAndOnlineDateFrameByProfile(onlineProfile.profileId!!)
                                        if (unfinishedDateFrame != null) {
                                            updateButtonToLoadingState()
                                            userViewModel.setUserOnline(foundUser)
                                            delay(1200) // JUST TO SIMULATE LOADING..
                                            findNavController().navigate(SigninFragmentDirections.actionGlobalMainSession())
                                        } else {
                                            userViewModel.setUserOnline(foundUser)
                                            delay(1200) // JUST TO SIMULATE LOADING..
                                            findNavController().navigate(SigninFragmentDirections.actionGlobalOnboardingSession())
                                        }
                                    } else {
                                        userViewModel.setUserOnline(foundUser)
                                        delay(1200) // JUST TO SIMULATE LOADING..
                                        findNavController().navigate(SigninFragmentDirections.actionGlobalOnBoardingStartFragment())
                                    }
                            } else {
                                showErrorMessage(resources.getString(R.string.error_message_signin_username_or_pass_wrong), binding)
                            }
                        }
                    }
                } else {
                    showErrorMessage(resources.getString(R.string.error_message_signin_empty_columns), binding)
                }
            }

            ivPasswordToHideFragSignIn.setOnClickListener { changePasswordVisibility("hide") }
            ivPasswordToShowFragSignIn.setOnClickListener { changePasswordVisibility("show") }

            tvSignUpFragSignIn.setOnClickListener {
                findNavController().navigate(SigninFragmentDirections.actionSigninFragmentToSignupFragment())
            }
        }

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(USERNAME_INPUT_KEY, binding.etUsernameFragSignIn.text.toString())
        outState.putString(PASSWORD_INPUT_KEY, binding.etPasswordFragSignIn.text.toString())

    }

    private fun changePasswordVisibility (state: String) {
        when (state) {
            "show" -> {
                binding.apply {
                    etPasswordFragSignIn.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    ivPasswordToHideFragSignIn.visibility = View.VISIBLE
                    ivPasswordToShowFragSignIn.visibility = View.INVISIBLE
                }
            }
            "hide" -> {
                binding.apply {
                    etPasswordFragSignIn.transformationMethod = PasswordTransformationMethod.getInstance()
                    ivPasswordToShowFragSignIn.visibility = View.VISIBLE
                    ivPasswordToHideFragSignIn.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun updateButtonToLoadingState () {
        binding.apply {
            btSignInFragSignIn.startAnimation(shrinkInsideAnim)
            ltLoadingFragSignIn.visibility = View.VISIBLE
            btSignInFragSignIn.isClickable = false
            btSignInFragSignIn.visibility = View.INVISIBLE
        }
    }
}