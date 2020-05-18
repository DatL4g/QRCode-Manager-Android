package de.datlag.qrcodemanager.util

import android.content.Context
import android.util.Log
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.Result
import de.datlag.qrcodemanager.R
import de.datlag.qrcodemanager.fragments.ScanFragment
import de.datlag.qrcodemanager.commons.*
import java.lang.Exception

class ScanManager(private val context: Context) : ScanFragment.Callback {

    private var scanResultShowing: Boolean = false

    override fun onScanned(result: Result) {
        scannerDialog(context.getString(R.string.scan_result), result.text)
    }

    override fun onFailure(exception: Exception?) {
        scannerDialog(context.getString(R.string.scan_failed), context.getString(R.string.scan_failed_message))
    }

    private fun scannerDialog(title: String, message: String) {
        if (!scanResultShowing) {
            val builder = MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setOnCancelListener { scanResultShowing = false }
                .setPositiveButton(context.getString(R.string.close)) { dialog, _ -> dialog.cancel() }
                .setNegativeButton(context.getString(R.string.copy)) { dialog, _ ->
                    dialog.cancel()
                    context.copyToClipboard(message)
                }

            val urls = message.getURLS()
            val linkBuilder = MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.open_link))
                .setItems(urls) { _, pos -> context.browserIntent(urls[pos]) }
                .setOnCancelListener { scanResultShowing = false }
                .setPositiveButton(context.getString(R.string.close)) { linkDialog, _ ->
                    linkDialog.dismiss()
                    scanResultShowing = true
                    builder.create().applyAnimation().apply {
                        setCanceledOnTouchOutside(true)
                        setOnCancelListener { scanResultShowing = false }
                    }.show()
                }

            when (contentChecker(message)) {
                CONTENT_LINK -> {
                    builder.setNeutralButton(context.getString(R.string.open_link)) { _, _ ->
                        context.browserIntent(urls[0])
                    }
                }
                CONTENT_LINKS -> {
                    builder.setNeutralButton(context.getString(R.string.open_link)) { dialog, _ ->
                        dialog.dismiss()
                        scanResultShowing = true
                        linkBuilder.create().applyAnimation().show()
                    }
                }
                CONTENT_WIFI -> {
                    builder.setNeutralButton(context.getString(R.string.save_network)) { dialog, _ ->
                        dialog.cancel()
                        NetworkManager().saveNetwork(context, message.getWiFiData())
                    }
                }
                CONTENT_EMAIL -> {
                    builder.setNeutralButton(context.getString(R.string.send_mail)) { dialog, _ ->
                        dialog.cancel()
                        context.startActivity(message.getMailIntent())
                    }
                }
                CONTENT_PHONE -> {
                    builder.setNeutralButton(context.getString(R.string.call_number)) { dialog, _ ->
                        dialog.cancel()
                        context.startActivity(message.getPhoneIntent())
                    }
                }
                CONTENT_SMS -> {
                    builder.setNeutralButton(context.getString(R.string.send_sms)) { dialog, _ ->
                        dialog.cancel()
                        context.startActivity(message.getSMSIntent())
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
            message.hasStartToken(*context.resources.getStringArray(R.array.wifi_tokens)) -> {
                return CONTENT_WIFI
            }
            message.hasStartToken(*context.resources.getStringArray(R.array.mail_tokens)) -> {
                return CONTENT_EMAIL
            }
            message.hasStartToken(*context.resources.getStringArray(R.array.number_tokens)) -> {
                return CONTENT_PHONE
            }
            message.hasStartToken(*context.resources.getStringArray(R.array.sms_tokens)) -> {
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