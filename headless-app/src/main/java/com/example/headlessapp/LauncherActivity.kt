package com.example.headlessapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Headless App의 런처 Activity
 * 앱 실행 시 WiFi 모니터링 서비스를 시작하고 Activity는 종료됩니다.
 */
class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 서비스 시작
        val serviceIntent = Intent(this, WifiStateService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        // Activity 종료 (백그라운드에서만 서비스 실행)
        finish()
    }
}
