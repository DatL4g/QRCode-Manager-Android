package de.datlag.qrcodemanager.commons

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.net.wifi.WifiManager

fun Context.browserIntent(url: String) {
    this.browserIntent(Uri.parse(url))
}

fun Context.browserIntent(uri: Uri) {
    val browserIntent = Intent(Intent.ACTION_VIEW, uri)
    this.startActivity(browserIntent)
}

fun Context.getClipboardManager(): ClipboardManager {
    return this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
}

fun Context.copyToClipboard(data: String, label: String = "null") {
    val clip = ClipData.newPlainText(label, data)
    getClipboardManager().setPrimaryClip(clip)
}

fun Context.getWifiManager(): WifiManager {
    return this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
}

fun Context.getConnectivityManager(): ConnectivityManager {
    return this.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
}
