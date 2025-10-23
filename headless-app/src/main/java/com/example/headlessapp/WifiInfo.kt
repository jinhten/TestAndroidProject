package com.example.headlessapp

import android.os.Parcel
import android.os.Parcelable

/**
 * WiFi 상태 정보를 담는 데이터 클래스
 * AIDL을 통해 전달되므로 Parcelable을 구현합니다.
 */
data class WifiInfo(
    val isEnabled: Boolean,
    val isConnected: Boolean,
    val ssid: String?,
    val rssi: Int,
    val linkSpeed: Int,
    val ipAddress: String?,
    val timestamp: Long
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (isEnabled) 1 else 0)
        parcel.writeByte(if (isConnected) 1 else 0)
        parcel.writeString(ssid)
        parcel.writeInt(rssi)
        parcel.writeInt(linkSpeed)
        parcel.writeString(ipAddress)
        parcel.writeLong(timestamp)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<WifiInfo> {
        override fun createFromParcel(parcel: Parcel): WifiInfo {
            return WifiInfo(parcel)
        }

        override fun newArray(size: Int): Array<WifiInfo?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return "WifiInfo(enabled=$isEnabled, connected=$isConnected, ssid=$ssid, " +
                "rssi=$rssi, linkSpeed=$linkSpeed, ip=$ipAddress, timestamp=$timestamp)"
    }
}
