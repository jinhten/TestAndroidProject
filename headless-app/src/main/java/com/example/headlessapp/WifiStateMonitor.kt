package com.example.headlessapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * WiFi 상태를 모니터링하고 저장하는 클래스
 */
class WifiStateMonitor(private val context: Context) {

    companion object {
        private const val TAG = "WifiStateMonitor"
    }

    private val wifiManager: WifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _wifiState = MutableStateFlow(createInitialWifiInfo())
    val wifiState: StateFlow<WifiInfo> = _wifiState.asStateFlow()

    private var isMonitoring = false

    // WiFi 상태 변화를 감지하는 BroadcastReceiver
    private val wifiStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                WifiManager.WIFI_STATE_CHANGED_ACTION,
                WifiManager.NETWORK_STATE_CHANGED_ACTION,
                WifiManager.RSSI_CHANGED_ACTION -> {
                    Log.d(TAG, "WiFi state changed: ${intent.action}")
                    updateWifiState()
                }
            }
        }
    }

    // Network Callback (Android 5.0+)
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "Network available")
            updateWifiState()
        }

        override fun onLost(network: Network) {
            Log.d(TAG, "Network lost")
            updateWifiState()
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            Log.d(TAG, "Network capabilities changed")
            updateWifiState()
        }
    }

    /**
     * WiFi 상태 모니터링 시작
     */
    fun startMonitoring() {
        if (isMonitoring) {
            Log.w(TAG, "Already monitoring")
            return
        }

        Log.i(TAG, "Starting WiFi monitoring")
        isMonitoring = true

        // BroadcastReceiver 등록
        val intentFilter = IntentFilter().apply {
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            addAction(WifiManager.RSSI_CHANGED_ACTION)
        }
        context.registerReceiver(wifiStateReceiver, intentFilter)

        // NetworkCallback 등록
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        // 초기 상태 업데이트
        updateWifiState()
    }

    /**
     * WiFi 상태 모니터링 중지
     */
    fun stopMonitoring() {
        if (!isMonitoring) {
            Log.w(TAG, "Not monitoring")
            return
        }

        Log.i(TAG, "Stopping WiFi monitoring")
        isMonitoring = false

        try {
            context.unregisterReceiver(wifiStateReceiver)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Error unregistering receiver", e)
        }

        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Error unregistering network callback", e)
        }
    }

    /**
     * 현재 WiFi 상태 가져오기
     */
    fun getCurrentWifiState(): WifiInfo = _wifiState.value

    /**
     * WiFi 상태 업데이트
     */
    private fun updateWifiState() {
        try {
            val newState = createWifiInfo()
            _wifiState.value = newState
            Log.d(TAG, "WiFi state updated: $newState")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating WiFi state", e)
        }
    }

    /**
     * 초기 WiFi 정보 생성
     */
    private fun createInitialWifiInfo(): WifiInfo {
        return WifiInfo(
            isEnabled = false,
            isConnected = false,
            ssid = null,
            rssi = 0,
            linkSpeed = 0,
            ipAddress = null,
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * 현재 WiFi 정보 생성
     */
    private fun createWifiInfo(): WifiInfo {
        val isEnabled = wifiManager.isWifiEnabled
        val isConnected = isWifiConnected()
        val connectionInfo = if (isEnabled) wifiManager.connectionInfo else null

        val ssid = if (isConnected && connectionInfo != null) {
            // SSID에서 따옴표 제거
            connectionInfo.ssid?.replace("\"", "")
        } else {
            null
        }

        val rssi = connectionInfo?.rssi ?: 0
        val linkSpeed = connectionInfo?.linkSpeed ?: 0
        val ipAddress = if (isConnected) getIpAddress() else null

        return WifiInfo(
            isEnabled = isEnabled,
            isConnected = isConnected,
            ssid = ssid,
            rssi = rssi,
            linkSpeed = linkSpeed,
            ipAddress = ipAddress,
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * WiFi 연결 여부 확인
     */
    private fun isWifiConnected(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork ?: return false
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                    ?: return false
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            } else {
                @Suppress("DEPRECATION")
                val networkInfo = connectivityManager.activeNetworkInfo
                networkInfo?.isConnected == true &&
                        networkInfo.type == ConnectivityManager.TYPE_WIFI
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking WiFi connection", e)
            false
        }
    }

    /**
     * IP 주소 가져오기
     */
    private fun getIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.name.startsWith("wlan")) {
                    val addresses = networkInterface.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val address = addresses.nextElement()
                        if (address is Inet4Address && !address.isLoopbackAddress) {
                            return address.hostAddress
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting IP address", e)
        }
        return null
    }
}
