package com.example.cashcontrol.util

import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.viewbinding.ViewBinding
import com.example.cashcontrol.R
import com.example.cashcontrol.ui.fragment.onboarding.OnBoardingFinishFragmentDirections
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

object MessageUtil {

    fun showErrorMessage(message: String, viewBinding: ViewBinding) {
        Snackbar.make(viewBinding.root, message, Snackbar.LENGTH_SHORT)
            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
            .setBackgroundTint(viewBinding.root.resources.getColor(R.color.bittersweet_red, null))
            .show()
    }

    fun showErrorMessage(message: String, viewBinding: ViewBinding, duration: Int) {
        Snackbar.make(viewBinding.root, message, duration)
            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
            .setBackgroundTint(viewBinding.root.resources.getColor(R.color.bittersweet_red, null))
            .show()
    }

    fun showErrorMessageWithButtonWithNavAction(message: String, buttonName: String, navController: NavController, direction: NavDirections, viewBinding: ViewBinding, duration: Int) {
        Snackbar.make(viewBinding.root, message, duration)
            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
            .setBackgroundTint(viewBinding.root.resources.getColor(R.color.bittersweet_red, null))
            .setAction(buttonName) {
                navController.navigate(direction)
            }
            .show()
    }

    fun showErrorMessageWithButton(message: String, buttonName: String, viewBinding: ViewBinding, duration: Int) {
        Snackbar.make(viewBinding.root, message, duration)
            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
            .setBackgroundTint(viewBinding.root.resources.getColor(R.color.bittersweet_red, null))
            .setAction(buttonName) {}
            .show()
    }

    fun showNotifyingMessage(message: String, viewBinding: ViewBinding) {
        Snackbar.make(viewBinding.root, message, Snackbar.LENGTH_SHORT)
            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
            .show()
    }

}