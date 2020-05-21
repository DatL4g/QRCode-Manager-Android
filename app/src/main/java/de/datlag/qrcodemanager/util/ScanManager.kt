package de.datlag.qrcodemanager.util

import android.app.Activity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.Result
import de.datlag.qrcodemanager.R
import de.datlag.qrcodemanager.commons.*
import de.datlag.qrcodemanager.fragments.ScanFragment

class ScanManager(private val activity: Activity) : ScanFragment.Callback {

    private var scanResultShowing: Boolean = false

    override fun onScanned(result: Result) {
        scannerDialog(activity.getString(R.string.scan_result), result.text)
    }

    override fun onFailure(exception: Exception?) {
        scannerDialog(activity.getString(R.string.scan_failed), activity.getString(R.string.scan_failed_message))
    }

    private fun scannerDialog(title: String, message: String) {
        if (!scanResultShowing) {
            val builder = MaterialAlertDialogBuilder(activity)
                .setTitle(title)
                .setMessage(message)
                .setOnCancelListener { scanResultShowing = false }
                .setPositiveButton(activity.getString(R.string.close)) { dialog, _ -> dialog.cancel() }
                .setNegativeButton(activity.getString(R.string.copy)) { dialog, _ ->
                    dialog.cancel()
                    activity.copyToClipboard(message)
                }

            val urls = message.getURLS()
            val linkBuilder = MaterialAlertDialogBuilder(activity)
                .setTitle(activity.getString(R.string.open_link))
                .setItems(urls) { _, pos -> activity.browserIntent(urls[pos]) }
                .setOnCancelListener { scanResultShowing = false }
                .setPositiveButton(activity.getString(R.string.close)) { linkDialog, _ ->
                    linkDialog.dismiss()
                    scanResultShowing = true
                    builder.create().applyAnimation().apply {
                        setCanceledOnTouchOutside(true)
                        setOnCancelListener { scanResultShowing = false }
                    }.show()
                }

            when (contentChecker(message)) {
                CONTENT_LINK -> {
                    builder.setNeutralButton(activity.getString(R.string.open_link)) { _, _ ->
                        activity.browserIntent(urls[0])
                    }
                }
                CONTENT_LINKS -> {
                    builder.setNeutralButton(activity.getString(R.string.open_link)) { dialog, _ ->
                        dialog.dismiss()
                        scanResultShowing = true
                        linkBuilder.create().applyAnimation().show()
                    }
                }
                CONTENT_WIFI -> {
                    builder.setNeutralButton(activity.getString(R.string.save_network)) { dialog, _ ->
                        dialog.cancel()
                        if (!activity.showInstall()) {
                            NetworkManager().saveNetwork(activity, message.getWiFiData())
                        }
                    }
                }
                CONTENT_EMAIL -> {
                    builder.setNeutralButton(activity.getString(R.string.send_mail)) { dialog, _ ->
                        dialog.cancel()
                        activity.startActivity(message.getMailIntent())
                    }
                }
                CONTENT_PHONE -> {
                    builder.setNeutralButton(activity.getString(R.string.call_number)) { dialog, _ ->
                        dialog.cancel()
                        activity.startActivity(message.getPhoneIntent())
                    }
                }
                CONTENT_SMS -> {
                    builder.setNeutralButton(activity.getString(R.string.send_sms)) { dialog, _ ->
                        dialog.cancel()
                        activity.startActivity(message.getSMSIntent())
                    }
                }
            }

            builder.create().applyAnimation().apply {
                setCanceledOnTouchOutside(true)
                setOnCancelListener { scanResultShowing = false }
            }.show()
            scanResultShowing = true
        }
    }

    private fun contentChecker(message: String): Int {
        when {
            message.containsURL() -> {
                return if (message.getURLS().size == 1) {
                    CONTENT_LINK
                } else {
                    CONTENT_LINKS
                }
            }
            message.hasStartToken(*activity.resources.getStringArray(R.array.wifi_tokens)) -> {
                return CONTENT_WIFI
            }
            message.hasStartToken(*activity.resources.getStringArray(R.array.mail_tokens)) -> {
                return CONTENT_EMAIL
            }
            message.hasStartToken(*activity.resources.getStringArray(R.array.number_tokens)) -> {
                return CONTENT_PHONE
            }
            message.hasStartToken(*activity.resources.getStringArray(R.array.sms_tokens)) -> {
                return CONTENT_SMS
            }
            else -> return CONTENT_NONE
        }
    }

    companion object {
        private const val CONTENT_NONE = 0
        private const val CONTENT_LINK = 1
        private const val CONTENT_LINKS = 2
        private const val CONTENT_WIFI = 3
        private const val CONTENT_EMAIL = 4
        private const val CONTENT_PHONE = 5
        private const val CONTENT_SMS = 6
    }

}