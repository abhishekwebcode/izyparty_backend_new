package com.izyparty.invitation.utils

import android.app.Activity
import android.os.Build
import android.support.v4.content.ContextCompat
import android.view.View
import com.izyparty.invitation.R

fun Activity.changeStatusBarColor(color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && color == R.color.white)
        window?.run {
            statusBarColor = ContextCompat.getColor(this@changeStatusBarColor, color)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    else
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this@changeStatusBarColor, color)
        }
}
fun changeStatusBarColor1(activity: Activity,color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && color == R.color.white)
        activity.window?.run {
            statusBarColor = ContextCompat.getColor(activity, color)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    else
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.statusBarColor = ContextCompat.getColor(activity, color)
        }
}