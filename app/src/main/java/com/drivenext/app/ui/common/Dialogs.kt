package com.drivenext.app.ui.common

import android.content.Context
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.drivenext.app.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun Context.showErrorDialog(message: String) {
    MaterialAlertDialogBuilder(this)
        .setTitle(R.string.error_title)
        .setMessage(message)
        .setPositiveButton(R.string.ok, null)
        .show()
}

fun Context.showErrorDialog(@StringRes messageRes: Int) = showErrorDialog(getString(messageRes))

fun Fragment.showErrorDialog(@StringRes messageRes: Int) = requireContext().showErrorDialog(messageRes)
