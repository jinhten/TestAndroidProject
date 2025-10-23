package com.example.headlessapp

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

/**
 * WiFi 상태를 제공하는 AIDL Service
 * 다른 앱에서 바인드하여 WiFi 상태 정보를 조회할 수 있습니다.
 */
class WifiStateService : Service() {

    companion object {
        private const val TAG = "WifiStateService"
    }

    private lateinit var wifiStateMonitor: WifiStateMonitor

    // AIDL 인터페이스 구현
    private val binder = object : IWifiStateService.Stub() {
        override fun getWifiState(): WifiInfo {
            Log.d(TAG, "getWifiState() called")
            val state = wifiStateMonitor.getCurrentWifiState()
            Log.d(TAG, "Returning WiFi state: $state")
            return state
        }

        override fun isWifiEnabled(): Boolean {
            Log.d(TAG, "isWifiEnabled() called")
            val enabled = wifiStateMonitor.getCurrentWifiState().isEnabled
            Log.d(TAG, "WiFi enabled: $enabled")
            return enabled
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Service created")

        // WifiStateMonitor 초기화 및 모니터링 시작
        wifiStateMonitor = WifiStateMonitor(this)
        wifiStateMonitor.startMonitoring()

        Log.i(TAG, "WiFi monitoring started")
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.i(TAG, "Service bound: ${intent?.action}")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "Service unbound")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Service destroyed")

        // 모니터링 중지
        wifiStateMonitor.stopMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Service started")
        // Service를 백그라운드에서 계속 실행
        return START_STICKY
    }
}
