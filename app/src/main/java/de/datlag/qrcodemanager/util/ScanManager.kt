package de.datlag.qrcodemanager.util

import android.content.Context
import android.util.Log
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.Result
import de.datlag.qrcodemanager.ScanFragment
import de.datlag.qrcodemanager.commons.*
import java.lang.Exception

class ScanManager(private val context: Context) : ScanFragment.Callback {

    private var scanResultShowing: Boolean = false

    override fun onScanned(result: Result) {
        scannerDialog("Scan Result", result.text)
    }

    override fun onFailure(exception: Exception?) {
        scannerDialog("Scan Failed", "Nothing found try a different image or try again")
    }

    private fun scannerDialog(title: String, message: String) {
        if (!scanResultShowing) {
            val builder = MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setOnCancelListener { scanResultShowing = false }
                .setPositiveButton("Close") { dialog, _ -> dialog.cancel() }
                .setNegativeButton("Copy") { dialog, _ ->
                    dialog.cancel()
                    context.copyToClipboard(message)
                }

            val urls = message.getURLS()
            val linkBuilder = MaterialAlertDialogBuilder(context)
                .setTitle("Open a link")
                .setItems(urls) { _, pos -> context.browserIntent(urls[pos]) }
                .setOnCancelListener { scanResultShowing = false }
                .setPositiveButton("Close") { linkDialog, _ ->
                    linkDialog.dismiss()
                    scanResultShowing = true
                    builder.create().applyAnimation().apply {
                        setCanceledOnTouchOutside(true)
                        setOnCancelListener { scanResultShowing = false }
                    }.show()
                }

            when (contentChecker(message)) {
                CONTENT_LINK -> {
                    builder.setNeutralButton("Open link") { _, _ ->
                        context.browserIntent(urls[0])
                    }
                }
                CONTENT_LINKS -> {
                    builder.setNeutralButton("Open link") { dialog, _ ->
                        dialog.dismiss()
                        scanResultShowing = true
                        linkBuilder.create().applyAnimation().show()
                    }
                }
                CONTENT_WIFI -> {
                    builder.setNeutralButton("Save Network") { dialog, _ ->
                        dialog.cancel()
                        Log.e("Wifi saved", NetworkManager().saveNetwork(context, message.getWiFiData()).toString())
                    }
                }
                CONTENT_EMAIL -> {
                    builder.setNeutralButton("Send Mail") { dialog, _ ->
                        dialog.cancel()
                        context.startActivity(message.getMailIntent())
                    }
                }
                CONTENT_PHONE -> {
                    builder.setNeutralButton("Call Number") { dialog, _ ->
                        dialog.cancel()
                        context.startActivity(message.getPhoneIntent())
                    }
                }
                CONTENT_SMS -> {
                    builder.setNeutralButton("Send SMS") { dialog, _ ->
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
            message.hasStartToken("wifi:") -> {
                return CONTENT_WIFI
            }
            message.hasStartToken("mailto:", "matmsg:") -> {
                return CONTENT_EMAIL
            }
            message.hasStartToken("tel:") -> {
                return CONTENT_PHONE
            }
            message.hasStartToken("smsto:") -> {
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