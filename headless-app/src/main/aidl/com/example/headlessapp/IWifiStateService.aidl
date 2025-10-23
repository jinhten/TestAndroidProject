package com.example.headlessapp;

import com.example.headlessapp.WifiInfo;

// WiFi 상태 조회를 위한 AIDL 인터페이스
interface IWifiStateService {
    /**
     * 현재 WiFi 상태를 반환합니다.
     * @return WifiInfo 객체 (WiFi 상태 정보)
     */
    WifiInfo getWifiState();

    /**
     * WiFi 활성화 여부를 반환합니다.
     * @return WiFi가 활성화되어 있으면 true, 아니면 false
     */
    boolean isWifiEnabled();
}
