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

#### 주요 기능
- AIDL 서비스 연결/해제 기능
- WiFi 상태 정보 조회 및 UI 표시
- 실시간 로그 표시
- Material Design UI

#### UI 구성
- **서비스 연결** 버튼: Headless App의 서비스에 바인드
- **서비스 연결 해제** 버튼: 서비스 바인딩 해제
- **WiFi 정보 조회** 버튼: 현재 WiFi 상태 가져오기
- **연결 상태**: 서비스 연결 여부 표시
- **WiFi 정보 카드**: WiFi 상태 정보 표시 (활성화, 연결, SSID, 신호강도, IP 등)
- **로그 영역**: 실시간 동작 로그 표시

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

### 1. Headless App 설치 및 실행
1. Headless App APK를 빌드하고 디바이스에 설치합니다
   ```bash
   ./gradlew :headless-app:assembleDebug
   adb install headless-app/build/outputs/apk/debug/headless-app-debug.apk
   ```

2. 서비스를 수동으로 시작합니다 (또는 재부팅 후 자동 시작)
   ```bash
   adb shell am start-foreground-service com.example.headlessapp/.WifiStateService
   ```

3. 앱이 백그라운드에서 WiFi 상태를 모니터링합니다

### 2. UI App 설치 및 실행
1. UI App APK를 빌드하고 디바이스에 설치합니다
   ```bash
   ./gradlew :ui-app:assembleDebug
   adb install ui-app/build/outputs/apk/debug/ui-app-debug.apk
   ```

2. UI App을 실행합니다

3. "서비스 연결" 버튼을 눌러 Headless App의 서비스에 연결합니다

4. "WiFi 정보 조회" 버튼을 눌러 현재 WiFi 상태를 확인합니다

### 주의사항
- Headless App이 먼저 설치되어 있어야 UI App에서 서비스에 연결할 수 있습니다
- Android 8.0(API 26) 이상에서는 위치 권한이 필요할 수 있습니다 (WiFi SSID 조회를 위해)
- 두 앱을 같은 디바이스에 모두 설치해야 정상 동작합니다

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
