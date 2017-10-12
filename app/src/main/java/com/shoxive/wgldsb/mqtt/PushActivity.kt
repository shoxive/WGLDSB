package com.shoxive.wgldsb.mqtt

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.shoxive.wgldsb.R
import java.text.SimpleDateFormat
import java.util.*

class PushActivity : Activity() {
    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null && !TextUtils.isEmpty(intent.action) && intent.action == "MQTT_MESSAGE") {
                if (mMessage != null) {
                    if (!TextUtils.isEmpty(mMessage!!.text.toString()) && mMessage!!.text.toString().split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size > 20) {
                        mMessage!!.text = "*************清空日志************\n"
                    }
                    mMessage!!.append(mDf.format(Date()) + intent.getStringExtra("message") + "\n")
                    if (intent.getIntExtra("needRetry", 0) == 1) {
                        mStartButton!!.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
    private val mDf = SimpleDateFormat("yyyy-MM-dd<HH:mm:ss>")
    private var mMessage: TextView? = null
    private var mEtContent: EditText? = null
    private var mStartButton: Button? = null
    private var mSendButton: Button? = null
    private var mConfig: MqttConfig? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.push_activity)
        mConfig = intent.getParcelableExtra<MqttConfig>("MQTT_CONFIG")
        Log.d("sssss", mConfig.toString())
        if (mConfig == null) {
            Toast.makeText(this, "OOPS,出错了！！", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        val filter = IntentFilter()
        filter.addAction("MQTT_MESSAGE")
        val serviceintent = Intent()
        serviceintent.action="shoixve.pushservice"
        serviceintent.`package`="com.shoxive.wgldsb"
        serviceintent.putExtra("MQTT_CONFIG", mConfig)
        registerReceiver(mReceiver, filter)
        mMessage = findViewById<TextView>(R.id.message)
        mEtContent = findViewById<EditText>(R.id.et_content)
        mStartButton = findViewById<Button>(R.id.start_button)
        mStartButton!!.setOnClickListener {
            startService(serviceintent)
            mStartButton!!.visibility = View.GONE
        }
        mSendButton = findViewById<Button>(R.id.btn_send)
        mSendButton!!.setOnClickListener {
            if (!TextUtils.isEmpty(mEtContent!!.text.toString())) {
                PushService.publish(mEtContent!!.text.toString())
                mEtContent!!.setText("")
            }
        }
        startService(serviceintent)
    }

    override fun onDestroy() {
        unregisterReceiver(mReceiver)
        val serviceintent = Intent()
        serviceintent.`package`="com.shoxive.wgldsb"
        serviceintent.action="shoixve.pushservice"
        stopService(serviceintent)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
    }
}