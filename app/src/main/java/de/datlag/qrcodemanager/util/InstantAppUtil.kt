package de.datlag.qrcodemanager.util

import android.app.Activity
import com.google.android.gms.instantapps.InstantApps

fun Activity.showInstall(code: Int  = INSTANT_APP_REQUEST_CODE): Boolean {
    if (InstantApps.getPackageManagerCompat(this).isInstantApp) {
        InstantApps.showInstallPrompt(this, null, code, null)
        return true
    }
    return false
}

const val INSTANT_APP_REQUEST_CODE = 1337