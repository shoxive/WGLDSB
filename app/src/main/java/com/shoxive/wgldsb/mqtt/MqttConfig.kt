package com.shoxive.wgldsb.mqtt

import android.os.Parcel
import android.os.Parcelable

/**
 * Mqtt配置信息
 * Created by shoxive on 17/10/11.
 */
data class MqttConfig(
        var mqtt_client_id: String,
        var mqtt_host: String,
        var mqtt_port: String,
        var user_name: String,
        var pass_word: String,
        var mqtt_topic: String) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(mqtt_client_id)
        writeString(mqtt_host)
        writeString(mqtt_port)
        writeString(user_name)
        writeString(pass_word)
        writeString(mqtt_topic)
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<MqttConfig> = object : Parcelable.Creator<MqttConfig> {
            override fun createFromParcel(source: Parcel): MqttConfig = MqttConfig(source)
            override fun newArray(size: Int): Array<MqttConfig?> = arrayOfNulls(size)
        }
    }
}