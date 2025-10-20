package com.sap.codelab.utils.extensions

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

fun Context.showToast(
    @StringRes messageRes: Int,
    length: Int = Toast.LENGTH_LONG,
) = showToast(
    message = getString(messageRes),
    length = length,
)

fun Context.showToast(
    message: String,
    length: Int = Toast.LENGTH_LONG,
) = Toast
    .makeText(this, message, length)
    .show()
