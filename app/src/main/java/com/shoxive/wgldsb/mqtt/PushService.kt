package com.shoxive.wgldsb.mqtt

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.IBinder
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

/**
 * MQTT长连接服务
 */
class PushService : Service() {
    //配置信息
    private var mConOptions: MqttConnectOptions? = null
    private var mConfig: MqttConfig? = null
    private var mReconnectNums = 0
    private var mServiceAlive: Boolean = true
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        mConfig = intent.getParcelableExtra<MqttConfig>("MQTT_CONFIG")
        Log.d("sssss", mConfig.toString())
        if (mConfig != null) {
            MQTT_HOST = "tcp://" + mConfig!!.mqtt_host + ":" + mConfig!!.mqtt_port
            USER_NAME = mConfig!!.user_name
            PASS_WORD = mConfig!!.pass_word
            TOPIC = mConfig!!.mqtt_topic
            CLIENT_ID = mConfig!!.mqtt_client_id
        }
        init()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun stopService(name: Intent?): Boolean {
        try {
            mClient!!.close()
            mClient!!.unregisterResources()
        } catch (e: MqttException) {
            e.printStackTrace()
        }
        mServiceAlive = false
        return super.stopService(name)
    }

    private fun init() {
        if (mClient != null && mClient!!.isConnected) {
            try {
                mClient!!.disconnect()
                mClient = null
            } catch (e: MqttException) {
                e.printStackTrace()
            }

        }
        mClient = MqttAndroidClient(this, MQTT_HOST, CLIENT_ID)
        mClient!!.setCallback(mqttCallback)
        mConOptions = MqttConnectOptions()
        //清缓存
        mConOptions!!.isCleanSession = true
        //超时时间 单位：秒
        mConOptions!!.connectionTimeout = 10
        //心跳间隔 单位：秒
        mConOptions!!.keepAliveInterval = 20
        mConOptions!!.userName = USER_NAME
        mConOptions!!.password = PASS_WORD.toCharArray()

        val message = "{\"terminal_uid\":\"$CLIENT_ID\"}"
        try {
            mConOptions!!.setWill(TOPIC, message.toByteArray(), 0, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        doClientConnection()
    }

    override fun onDestroy() {
        mServiceAlive = false
        try {
            mClient!!.close()
            mClient!!.unregisterResources()
        } catch (e: MqttException) {
            e.printStackTrace()
        }

        super.onDestroy()
    }

    /**
     * 连接mqtt服务器
     */
    private fun doClientConnection() {
        if (!mServiceAlive) {
            return
        }
        if (mReconnectNums >= 10) {
            val intent = Intent("MQTT_MESSAGE")
            intent.putExtra("message", "重连失败超过10次，请重新启动")
            intent.putExtra("needRetry", "1")
            sendBroadcast(intent)
            this@PushService.stopSelf()
            return
        }
        if (mClient!=null&&!mClient!!.isConnected && isConnectIsNomarl) {
            try {
                mClient!!.connect(mConOptions, null, iMqttActionListener)
                mReconnectNums++
            } catch (e: MqttException) {
                e.printStackTrace()
            }

        }
    }

    // MQTT是否连接成功
    private val iMqttActionListener = object : IMqttActionListener {

        override fun onSuccess(arg0: IMqttToken) {
            Log.i(TAG, "连接成功 ")
            val intent = Intent("MQTT_MESSAGE")
            intent.putExtra("message", "连接成功")
            sendBroadcast(intent)
            mReconnectNums = 0
            try {
                // 订阅myTopic话题
                mClient!!.subscribe(TOPIC, 1)
            } catch (e: MqttException) {
                e.printStackTrace()
            }

        }

        override fun onFailure(arg0: IMqttToken, arg1: Throwable) {
            arg1.printStackTrace()
            val intent = Intent("MQTT_MESSAGE")
            intent.putExtra("message", "连接失败，重连")
            sendBroadcast(intent)
            // 连接失败，重连
            doClientConnection()
        }
    }
    // MQTT监听并且接受消息
    private val mqttCallback = object : MqttCallback {

        @Throws(Exception::class)
        override fun messageArrived(topic: String, message: MqttMessage) {
            val str1 = String(message.payload)
            val intent = Intent("MQTT_MESSAGE")
            intent.putExtra("message", str1)
            sendBroadcast(intent)
        }

        override fun deliveryComplete(arg0: IMqttDeliveryToken) {

        }

        override fun connectionLost(arg0: Throwable?) {
            // 失去连接，重连
//            val intent = Intent("MQTT_MESSAGE")
//            intent.putExtra("message", "连接丢失，重连")
//            sendBroadcast(intent)
        }
    }

    /**
     * 判断网络是否连接
     */
    private val isConnectIsNomarl: Boolean
        get() {
            val connectivityManager = this.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val info = connectivityManager.activeNetworkInfo
            if (info != null && info.isAvailable) {
                val name = info.typeName
                Log.i(TAG, "MQTT当前网络名称：" + name)
                return true
            } else {
                Log.i(TAG, "MQTT 没有可用网络")
                return false
            }
        }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        val TAG = "PushService"
        private var mClient: MqttAndroidClient? = null
        private var MQTT_HOST = "tcp://139.196.101.244:1883"
        private var USER_NAME = "leon_led"
        private var PASS_WORD = "leon_led@led123"
        private var TOPIC = "led/rgb"
        private var CLIENT_ID = "FUCKIOS"

        fun publish(msg: String) {
            try {
                mClient!!.publish(TOPIC, msg.toByteArray(), 0, false)
            } catch (e: MqttException) {
                e.printStackTrace()
            }

        }

        fun reConnect() {}
    }

}