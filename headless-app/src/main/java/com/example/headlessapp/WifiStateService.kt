package com.example.headlessapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log

/**
 * WiFi 상태를 제공하는 AIDL Service
 * 다른 앱에서 바인드하여 WiFi 상태 정보를 조회할 수 있습니다.
 */
class WifiStateService : Service() {

    companion object {
        private const val TAG = "WifiStateService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "wifi_state_service_channel"
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

        // Foreground Service로 시작
        startForeground(NOTIFICATION_ID, createNotification())

        // WifiStateMonitor 초기화 및 모니터링 시작
        wifiStateMonitor = WifiStateMonitor(this)
        wifiStateMonitor.startMonitoring()

        Log.i(TAG, "WiFi monitoring started")
    }

    /**
     * Foreground Service용 Notification 생성
     */
    private fun createNotification(): Notification {
        // Android 8.0 이상에서는 Notification Channel 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "WiFi State Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "WiFi 상태 모니터링 서비스"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        return builder
            .setContentTitle("WiFi 모니터링 서비스")
            .setContentText("WiFi 상태를 모니터링하고 있습니다")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .build()
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
        // Foreground Service로 시작
        startForeground(NOTIFICATION_ID, createNotification())
        // Service를 백그라운드에서 계속 실행
        return START_STICKY
    }
}
