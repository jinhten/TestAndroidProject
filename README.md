# TestAndroidProject

Android IPC (Inter-Process Communication) 예제 프로젝트입니다. AIDL을 사용하여 두 개의 앱 간에 WiFi 상태 정보를 공유합니다.

## 프로젝트 구조

이 프로젝트는 2개의 Android 앱으로 구성되어 있습니다:

### 1. Headless App (`headless-app`)
- UI가 없이 백그라운드에서 동작하는 앱
- WiFi 상태를 실시간으로 모니터링하고 저장
- AIDL Service를 통해 다른 앱에 WiFi 상태 정보 제공
- 부팅 시 자동으로 시작

#### 주요 기능
- WiFi 상태 실시간 모니터링 (연결/해제, SSID, 신호 강도 등)
- AIDL 서비스를 통한 WiFi 정보 제공
- 백그라운드에서 지속적으로 실행

#### 주요 컴포넌트
- `WifiStateService`: AIDL 서비스 구현
- `WifiStateMonitor`: WiFi 상태 모니터링
- `WifiInfo`: WiFi 상태 정보 데이터 클래스
- `IWifiStateService.aidl`: AIDL 인터페이스 정의
- `BootReceiver`: 부팅 시 서비스 자동 시작

### 2. UI App (`ui-app`)
- 사용자 인터페이스를 가진 앱
- Headless App의 AIDL 서비스에 바인드하여 WiFi 상태 조회
- 조회한 WiFi 정보를 UI에 표시

#### 주요 기능 (예정)
- WiFi 상태 정보 UI 표시
- 실시간 WiFi 정보 업데이트
- AIDL을 통한 Headless App과의 통신

## AIDL 인터페이스

### IWifiStateService
```java
interface IWifiStateService {
    WifiInfo getWifiState();      // 현재 WiFi 상태 반환
    boolean isWifiEnabled();       // WiFi 활성화 여부 반환
}
```

### WifiInfo
WiFi 상태 정보를 담는 Parcelable 클래스:
- `isEnabled`: WiFi 활성화 여부
- `isConnected`: WiFi 연결 여부
- `ssid`: 연결된 WiFi SSID
- `rssi`: 신호 강도
- `linkSpeed`: 링크 속도 (Mbps)
- `ipAddress`: IP 주소
- `timestamp`: 상태 확인 시간

## 필요한 권한

### Headless App
```xml
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
```

## 빌드 방법

```bash
# 전체 프로젝트 빌드
./gradlew build

# Headless App만 빌드
./gradlew :headless-app:build

# UI App만 빌드
./gradlew :ui-app:build

# APK 생성
./gradlew :headless-app:assembleDebug
./gradlew :ui-app:assembleDebug
```

## 개발 환경
- Kotlin 1.9.20
- Android Gradle Plugin 8.2.0
- Gradle 8.2
- compileSdk: 34
- minSdk: 24
- targetSdk: 34

## 사용 방법

1. Headless App 설치 및 실행
   - Headless App을 먼저 설치합니다
   - 앱이 백그라운드에서 자동으로 WiFi 상태를 모니터링합니다

2. UI App 설치 및 실행 (개발 예정)
   - UI App을 설치합니다
   - 앱을 실행하면 Headless App의 서비스에 바인드됩니다
   - WiFi 상태 정보가 UI에 표시됩니다

## 아키텍처

```
┌─────────────────────┐          AIDL          ┌─────────────────────┐
│    UI App           │ ◄──────────────────────► │  Headless App       │
│  (ui-app)           │      IWifiStateService  │  (headless-app)     │
├─────────────────────┤                         ├─────────────────────┤
│ - MainActivity      │                         │ - WifiStateService  │
│ - ServiceConnection │                         │ - WifiStateMonitor  │
│ - UI Display        │                         │ - BootReceiver      │
└─────────────────────┘                         └─────────────────────┘
                                                          │
                                                          ▼
                                                   ┌─────────────┐
                                                   │ WiFi System │
                                                   └─────────────┘
```

## 라이센스
이 프로젝트는 학습 및 테스트 목적으로 작성되었습니다.
