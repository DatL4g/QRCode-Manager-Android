package de.datlag.qrcodemanager.commons

import android.os.Build
import android.view.Window
import android.view.WindowManager

fun Window.applyStatusBarColor(color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        this.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        this.statusBarColor = color
    }
}