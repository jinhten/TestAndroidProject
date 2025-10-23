package com.example.uiapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.headlessapp.IWifiStateService
import com.example.headlessapp.WifiInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * WiFi 상태 정보를 표시하는 메인 액티비티
 * Headless App의 AIDL 서비스에 바인딩하여 WiFi 정보를 조회하고 UI에 표시합니다.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val HEADLESS_APP_PACKAGE = "com.example.headlessapp"
        private const val SERVICE_ACTION = "com.example.headlessapp.WIFI_STATE_SERVICE"
    }

    // View References
    private lateinit var btnConnect: android.widget.Button
    private lateinit var btnDisconnect: android.widget.Button
    private lateinit var btnGetWifiInfo: android.widget.Button
    private lateinit var connectionStatusText: android.widget.TextView
    private lateinit var wifiInfoText: android.widget.TextView
    private lateinit var logText: android.widget.TextView

    // Service Connection
    private var wifiStateService: IWifiStateService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "Service connected: $name")
            wifiStateService = IWifiStateService.Stub.asInterface(service)
            isBound = true
            updateConnectionStatus(true)
            addLog("서비스 연결 성공")
            Toast.makeText(
                this@MainActivity,
                R.string.msg_service_connected,
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "Service disconnected: $name")
            wifiStateService = null
            isBound = false
            updateConnectionStatus(false)
            addLog("서비스 연결 끊김")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupListeners()
        addLog("앱 시작")
    }

    /**
     * View 초기화
     */
    private fun initViews() {
        btnConnect = findViewById(R.id.btnConnect)
        btnDisconnect = findViewById(R.id.btnDisconnect)
        btnGetWifiInfo = findViewById(R.id.btnGetWifiInfo)
        connectionStatusText = findViewById(R.id.connectionStatusText)
        wifiInfoText = findViewById(R.id.wifiInfoText)
        logText = findViewById(R.id.logText)
    }

    /**
     * 버튼 리스너 설정
     */
    private fun setupListeners() {
        btnConnect.setOnClickListener {
            connectToService()
        }

        btnDisconnect.setOnClickListener {
            disconnectFromService()
        }

        btnGetWifiInfo.setOnClickListener {
            getWifiInfo()
        }
    }

    /**
     * Headless App의 WiFi State Service에 연결
     */
    private fun connectToService() {
        if (isBound) {
            addLog("이미 서비스에 연결되어 있습니다")
            return
        }

        try {
            val intent = Intent(SERVICE_ACTION).apply {
                setPackage(HEADLESS_APP_PACKAGE)
            }

            val success = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            if (success) {
                addLog("서비스 바인딩 시도 중...")
            } else {
                addLog("서비스 바인딩 실패")
                Toast.makeText(
                    this,
                    R.string.error_bind_failed,
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding service", e)
            addLog("에러: ${e.message}")
            Toast.makeText(
                this,
                R.string.error_headless_app_not_installed,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * 서비스 연결 해제
     */
    private fun disconnectFromService() {
        if (!isBound) {
            addLog("서비스가 연결되어 있지 않습니다")
            return
        }

        try {
            unbindService(serviceConnection)
            wifiStateService = null
            isBound = false
            updateConnectionStatus(false)
            addLog("서비스 연결 해제됨")
            Toast.makeText(
                this,
                R.string.msg_service_disconnected,
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error unbinding service", e)
            addLog("에러: ${e.message}")
        }
    }

    /**
     * WiFi 정보 조회
     */
    private fun getWifiInfo() {
        if (!isBound || wifiStateService == null) {
            Toast.makeText(
                this,
                R.string.error_service_not_connected,
                Toast.LENGTH_SHORT
            ).show()
            addLog("서비스가 연결되지 않았습니다")
            return
        }

        try {
            addLog("WiFi 정보 조회 중...")
            val wifiInfo = wifiStateService?.getWifiState()
            if (wifiInfo != null) {
                displayWifiInfo(wifiInfo)
                addLog("WiFi 정보 조회 성공")
            } else {
                addLog("WiFi 정보가 null입니다")
                wifiInfoText.text = getString(R.string.no_data)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting WiFi info", e)
            addLog("에러: ${e.message}")
            wifiInfoText.text = "오류 발생: ${e.message}"
        }
    }

    /**
     * WiFi 정보를 UI에 표시
     */
    private fun displayWifiInfo(info: WifiInfo) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val timestamp = dateFormat.format(Date(info.timestamp))

        val displayText = buildString {
            append("WiFi 활성화: ${if (info.isEnabled) "예" else "아니오"}\n")
            append("WiFi 연결: ${if (info.isConnected) "예" else "아니오"}\n")
            append("\n")
            if (info.isConnected) {
                append("SSID: ${info.ssid ?: "알 수 없음"}\n")
                append("신호 강도: ${info.rssi} dBm\n")
                append("링크 속도: ${info.linkSpeed} Mbps\n")
                append("IP 주소: ${info.ipAddress ?: "알 수 없음"}\n")
            } else {
                append("연결된 네트워크가 없습니다\n")
            }
            append("\n")
            append("조회 시간: $timestamp")
        }

        wifiInfoText.text = displayText
        Log.d(TAG, "WiFi Info: $info")
    }

    /**
     * 연결 상태 업데이트
     */
    private fun updateConnectionStatus(connected: Boolean) {
        runOnUiThread {
            if (connected) {
                connectionStatusText.text = getString(R.string.status_connected)
                btnConnect.isEnabled = false
                btnDisconnect.isEnabled = true
                btnGetWifiInfo.isEnabled = true
            } else {
                connectionStatusText.text = getString(R.string.status_disconnected)
                btnConnect.isEnabled = true
                btnDisconnect.isEnabled = false
                btnGetWifiInfo.isEnabled = false
            }
        }
    }

    /**
     * 로그 추가
     */
    private fun addLog(message: String) {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val timestamp = timeFormat.format(Date())
        val logMessage = "[$timestamp] $message\n"

        runOnUiThread {
            logText.append(logMessage)
            // 스크롤을 최하단으로 이동
            val scrollView = logText.parent as? android.widget.ScrollView
            scrollView?.post {
                scrollView.fullScroll(android.view.View.FOCUS_DOWN)
            }
        }

        Log.d(TAG, message)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Activity 종료 시 서비스 연결 해제
        if (isBound) {
            try {
                unbindService(serviceConnection)
            } catch (e: Exception) {
                Log.e(TAG, "Error unbinding service in onDestroy", e)
            }
        }
    }
}
