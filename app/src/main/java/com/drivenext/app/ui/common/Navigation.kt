package com.drivenext.app.ui.common

import android.app.Activity
import android.content.Intent

fun Activity.openScreen(screen: Class<out Activity>) {
    startActivity(Intent(this, screen))
}

fun Activity.openScreenAndClearBackStack(screen: Class<out Activity>) {
    val intent = Intent(this, screen)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    startActivity(intent)
}
