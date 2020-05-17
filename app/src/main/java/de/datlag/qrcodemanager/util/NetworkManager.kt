package de.datlag.qrcodemanager.util

import android.annotation.TargetApi
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import androidx.annotation.RequiresApi
import de.datlag.qrcodemanager.commons.getConnectivityManager
import de.datlag.qrcodemanager.commons.getWifiManager
import de.datlag.qrcodemanager.commons.toLower
import org.json.JSONObject

@Suppress("DEPRECATION")
class NetworkManager {

    fun saveNetwork(context: Context, json: JSONObject): Boolean {
        val security: Int = when (json.getString("secure").toLower()) {
            "wep" -> {
                WIFI_SECURITY_WEP
            }
            "wpa" -> {
                WIFI_SECURITY_WPA
            }
            else -> {
                WIFI_SECURITY_NONE
            }
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && security != WIFI_SECURITY_WPA) {
            false
        } else {
            saveWifiConfig(context, json, security)
        }
    }

    private fun saveWifiConfig(context: Context, json: JSONObject, security: Int): Boolean {
        val wifiManager = context.getWifiManager()
        val connectivityManager = context.getConnectivityManager()

        wifiManager.isWifiEnabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            connectivityManager.requestNetwork(createNetworkSpecifier(json), object: ConnectivityManager.NetworkCallback() {})
            return true
        } else {
            val wifiConfig = createWifiConfig(json, security)
            var networkId = wifiManager.addNetwork(wifiConfig)

            if (networkId == -1) {
                if (security == WIFI_SECURITY_WEP) {
                    wifiConfig.wepKeys[0] = json.getString("password")
                    networkId = wifiManager.addNetwork(wifiConfig)
                    return networkId != -1
                } else if (security == WIFI_SECURITY_WPA) {
                    wifiConfig.preSharedKey = json.getString("password")
                    networkId = wifiManager.addNetwork(wifiConfig)
                    return networkId != -1
                }
                return false
            } else {
                return true
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.Q)
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createNetworkSpecifier(json: JSONObject): NetworkRequest {
        val ssid = json.getString("ssid")
        val password = json.getString("password")
        val hidden = json.getBoolean("hidden")

        val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(password)
            .setWpa3Passphrase(password)
            .setIsHiddenSsid(hidden)
            .build()

        return NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(wifiNetworkSpecifier)
            .build()
    }

    private fun createWifiConfig(json: JSONObject, security: Int): WifiConfiguration {
        val ssid = json.getString("ssid")
        val password = json.getString("password")
        val hidden = json.getBoolean("hidden")
        val wifiConfig = WifiConfiguration()

        wifiConfig.allowedAuthAlgorithms.clear()
        wifiConfig.allowedGroupCiphers.clear()
        wifiConfig.allowedKeyManagement.clear()
        wifiConfig.allowedPairwiseCiphers.clear()
        wifiConfig.allowedProtocols.clear()

        wifiConfig.SSID = String.format("\"%s\"", ssid)
        when(security) {
            WIFI_SECURITY_NONE -> {
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            }
            WIFI_SECURITY_WEP -> {
                wifiConfig.wepKeys[0] = String.format("\"%s\"", password)
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                wifiConfig.wepTxKeyIndex = 0
            }
            WIFI_SECURITY_WPA -> {
                wifiConfig.preSharedKey = String.format("\"%s\"", password)
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                wifiConfig.status = WifiConfiguration.Status.ENABLED
            }
        }
        wifiConfig.hiddenSSID = hidden
        return wifiConfig
    }

    companion object {
        private const val WIFI_SECURITY_NONE = 0
        private const val WIFI_SECURITY_WEP = 1
        private const val WIFI_SECURITY_WPA = 2
    }

}