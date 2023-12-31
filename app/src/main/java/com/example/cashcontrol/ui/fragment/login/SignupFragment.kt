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
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.cashcontrol.R
import com.example.cashcontrol.data.db.entity.User
import com.example.cashcontrol.databinding.FragmentSignupBinding
import com.example.cashcontrol.ui.viewmodel.UserViewModel
import com.example.cashcontrol.util.MessageUtil.showErrorMessage
import com.example.cashcontrol.util.constant.UIStateConstant.PASSWORD_INPUT_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.PASSWORD_REENTER_INPUT_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.USERNAME_INPUT_KEY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignupFragment : Fragment() {
    private lateinit var binding: FragmentSignupBinding
    private lateinit var userViewModel: UserViewModel
    private val shrinkButtonAnim: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.shrink_button_anim) }
    private val expandButtonAnim: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.expand_button_anim) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel = ViewModelProvider (requireActivity())[UserViewModel::class.java]
        binding = FragmentSignupBinding.inflate(layoutInflater)

        if (savedInstanceState != null) {
            binding.apply {
                etUsernameFragSignUp.setText(savedInstanceState.getString(USERNAME_INPUT_KEY))
                etPasswordFragSignUp.setText(savedInstanceState.getString(PASSWORD_INPUT_KEY))
                etReenterPasswordFragSignUp.setText(savedInstanceState.getString(PASSWORD_REENTER_INPUT_KEY))
            }
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

        binding.apply {
            btSignUpFragSignUp.setOnClickListener {
                val username = binding.etUsernameFragSignUp.text.toString()
                val password = binding.etPasswordFragSignUp.text.toString()
                val passwordReenter = binding.etReenterPasswordFragSignUp.text.toString()

                viewLifecycleOwner.lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        if (username.isNotEmpty() && password.isNotEmpty() && passwordReenter.isNotEmpty()) {
                            buttonLoadingState(true)
                            delay(500) // JUST TO SIMULATE LOADING..
                            if (password == passwordReenter) {
                                if (password.length > 5) {
                                    if (userViewModel.isUsernameValid(username)) {
                                        userViewModel.upsertUser(User(username, password, true, mutableSetOf(), mutableSetOf()))
                                        delay(300)
                                        findNavController().navigate(SignupFragmentDirections.actionGlobalOnBoardingStartFragment())
                                    } else {
                                        showErrorMessage(resources.getString(R.string.error_message_signup_used_username), binding)
                                        buttonLoadingState(false)
                                    }
                                } else {
                                    showErrorMessage(resources.getString(R.string.error_message_signup_password_length), binding)
                                    buttonLoadingState(false)
                                }
                            } else {
                                showErrorMessage(resources.getString(R.string.error_message_signup_non_macthing_passwords), binding)
                                buttonLoadingState(false)
                            }
                        } else {
                            showErrorMessage(resources.getString(R.string.error_message_signup_empty_columns), binding)
                        }
                    }
                }
            }

            ivPasswordToShowFragSignUp.setOnClickListener { changePasswordVisibility("show") }
            ivPasswordToHideFragSignUp.setOnClickListener { changePasswordVisibility("hide") }

            tvSignInFragSignUp.setOnClickListener {
                findNavController().popBackStack()
            }
        }

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(USERNAME_INPUT_KEY, binding.etUsernameFragSignUp.text.toString())
        outState.putString(PASSWORD_INPUT_KEY, binding.etPasswordFragSignUp.text.toString())
        outState.putString(PASSWORD_REENTER_INPUT_KEY, binding.etReenterPasswordFragSignUp.text.toString())

    }

    private fun changePasswordVisibility (state: String) {
        when (state) {
            "show" -> {
                binding.apply {
                    etPasswordFragSignUp.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    etReenterPasswordFragSignUp.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    ivPasswordToHideFragSignUp.visibility = View.VISIBLE
                    ivPasswordToShowFragSignUp.visibility = View.INVISIBLE
                }
            }
            "hide" -> {
                binding.apply {
                    etPasswordFragSignUp.transformationMethod = PasswordTransformationMethod.getInstance()
                    etReenterPasswordFragSignUp.transformationMethod = PasswordTransformationMethod.getInstance()
                    ivPasswordToShowFragSignUp.visibility = View.VISIBLE
                    ivPasswordToHideFragSignUp.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun buttonLoadingState (isLoading: Boolean) {
        binding.apply {
            when (isLoading) {
                true -> {
                    btSignUpFragSignUp.startAnimation(shrinkButtonAnim)
                    ltLoadingFragSignUp.visibility = View.VISIBLE
                    btSignUpFragSignUp.isClickable = false
                    btSignUpFragSignUp.visibility = View.INVISIBLE
                }
                else -> {
                    btSignUpFragSignUp.startAnimation(expandButtonAnim)
                    ltLoadingFragSignUp.visibility = View.INVISIBLE
                    btSignUpFragSignUp.isClickable = true
                    btSignUpFragSignUp.visibility = View.VISIBLE
                }
            }
        }
    }
}