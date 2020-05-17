package de.datlag.qrcodemanager.commons

import android.content.Intent
import android.net.Uri
import org.json.JSONObject
import java.net.URLDecoder
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList

fun String.containsURL(): Boolean {
    val matcher = linkMatcher(this)
    return matcher.find()
}

fun String.getURLS(): Array<String> {
    val list = mutableListOf<String>()
    val matcher = linkMatcher(this)
    while (matcher.find()) {
        list.add(matcher.group())
    }
    return list.toTypedArray()
}

private fun linkMatcher(text: String): Matcher {
    val regexString = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"
    val pattern = Pattern.compile(regexString, Pattern.CASE_INSENSITIVE)
    return pattern.matcher(text)
}

fun String.hasStartToken(vararg tokens: String): Boolean {
    var contains = false
    for (token in tokens) {
        if (this.toLower().startsWith(token.toLower(), true)) {
            contains = true
            break
        }
    }

    return contains
}

fun String.subAfter(delimiter: String, ignoreCase: Boolean = false): String {
    return if (!ignoreCase) {
        this.substringAfter(delimiter)
    } else {
        val pattern = Pattern.compile(delimiter, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(this).apply { find() }
        this.substring(matcher.end())
    }
}

fun String.getWiFiData(): JSONObject {
    val cut = this.subAfter("wifi:", true)
    val split = cut.split(";")

    val wifiMap = mutableMapOf<String, Any>()
    wifiMap["ssid"] = ""
    wifiMap["secure"] = ""
    wifiMap["password"] = ""
    wifiMap["hidden"] = false
    for (part in split) {
        when {
            part.startsWith("S:") -> {
                wifiMap["ssid"] = getWiFiToken(part, "S:")
            }
            part.startsWith("T:") -> {
                wifiMap["secure"] = getWiFiToken(part, "T:")
            }
            part.startsWith("P:") -> {
                wifiMap["password"] = getWiFiToken(part, "P:")
            }
            part.startsWith("H:") -> {
                wifiMap["hidden"] = getWiFiToken(part, "H:").toBoolean()
            }
        }
    }

    return JSONObject(wifiMap as Map<*, *>)
}

private fun getWiFiToken(data: String, token: String): String {
    return data.subAfter(token, true)
}

fun String.getMailIntent(): Intent {
    val cut = if (this.hasStartToken("matmsg:")) {
        this.subAfter("matmsg:", true)
    } else {
        this.subAfter("mailto:", true)
    }.replace("to:", String(), true).replace("?", "&")


    val toList = ArrayList<String>()
    val ccList = ArrayList<String>()
    val bccList = ArrayList<String>()
    var subject = String()
    var body = String()

    val urlSections = cut.split("[&;]".toRegex())

    if (urlSections.size > 2) {
        toList.addAll(urlSections[0].split(","))

        for (urlSection in urlSections) {
            val keyValues = urlSection.split("[=:]".toRegex())

            if (keyValues.size == 2) {
                val key = keyValues[0]
                val value = try {
                    URLDecoder.decode(keyValues[1], "UTF-8")
                } catch (ignored: Exception) {
                    keyValues[1]
                }

                when(key.toLower()) {
                    "cc" -> {
                        ccList.addAll(value.split(","))
                    }
                    "bcc" -> {
                        bccList.addAll(value.split(","))
                    }
                    "subject" -> {
                        subject = value
                    }
                    "sub" -> {
                        subject = value
                    }
                    "body" -> {
                        body = value
                    }
                }
            }
        }
    } else {
        toList.addAll(cut.split(","))
    }

    val emailIntent = Intent(Intent.ACTION_SEND)
    emailIntent.type = "message/rfc822"
    emailIntent.putExtra(Intent.EXTRA_EMAIL, toList.toTypedArray())
    if (ccList.size > 0) {
        emailIntent.putExtra(Intent.EXTRA_CC, ccList.toTypedArray())
    }
    if (bccList.size > 0) {
        emailIntent.putExtra(Intent.EXTRA_BCC, bccList.toTypedArray())
    }
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
    emailIntent.putExtra(Intent.EXTRA_TEXT, body)
    return emailIntent
}

fun String.getPhoneIntent(): Intent {
    return Intent(Intent.ACTION_DIAL).apply { data = Uri.parse(this@getPhoneIntent) }
}

fun String.getSMSIntent(): Intent {
    val cut = this.subAfter("smsto:", true)
    val sections = cut.split(":")
    return Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${sections[0]}")).apply {
        if (sections.size > 1) {
            putExtra("sms_body", sections[1])
        }
    }
}

fun String.toLower() = toLowerCase(Locale.getDefault())