package de.datlag.qrcodemanager.commons

import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import de.datlag.qrcodemanager.R

fun AlertDialog.applyAnimation(@StyleRes animation: Int? = null): AlertDialog {
    if (animation == null) {
        applyAnimation(R.style.MaterialDialogAnimation)
    } else {
        window?.attributes?.windowAnimations = animation
    }
    return this
}

fun android.app.AlertDialog.applyAnimation(@StyleRes animation: Int? = null) {
    if (animation == null) {
        applyAnimation(R.style.MaterialDialogAnimation)
    } else {
        window?.attributes?.windowAnimations = animation
    }
}
