package com.shoxive.wgldsb

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import com.shoxive.wgldsb.mqtt.MqttConfig
import com.shoxive.wgldsb.mqtt.MqttConfigList
import com.shoxive.wgldsb.mqtt.PushActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var list=ArrayList<MqttConfig>()
    lateinit var adapter: RecycleAdapter
    @SuppressLint("ApplySharedPref")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //初始化适配器
        initData()
        adapter = RecycleAdapter(this,list)
        recycler_view.layoutManager = LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        recycler_view.adapter = adapter
        btn_add.setOnClickListener {
            val dialog: Dialog = Dialog(this,android.R.style.Theme_Holo_Dialog)
            val view: View = LayoutInflater.from(this).inflate(R.layout.layout_dialog,null)
            dialog.setContentView(view)
            val et_title: EditText=view.findViewById(R.id.et_title)
            val et_host: EditText=view.findViewById(R.id.et_host)
            val et_port: EditText=view.findViewById(R.id.et_port)
            val et_user_name: EditText=view.findViewById(R.id.et_user_name)
            val et_pwd: EditText=view.findViewById(R.id.et_pwd)
            val et_topic: EditText=view.findViewById(R.id.et_topic)
            view.findViewById<Button>(R.id.btn_confirm).setOnClickListener {
                val config: MqttConfig = MqttConfig(
                        et_title.text.toString(),
                        et_host.text.toString(),
                        et_port.text.toString(),
                        et_user_name.text.toString(),
                        et_pwd.text.toString(),
                        et_topic.text.toString()
                        )
                if(!TextUtils.isEmpty(config.mqtt_client_id)&&!TextUtils.isEmpty(config.mqtt_host)&&!TextUtils.isEmpty(config.mqtt_port)&&!TextUtils.isEmpty(config.user_name)&&!TextUtils.isEmpty(config.pass_word)&&!TextUtils.isEmpty(config.mqtt_topic)){
                    list.add(config)
                    var mqttlist: MqttConfigList = MqttConfigList(list)
                    getSharedPreferences("mqtt_config",Context.MODE_PRIVATE).edit().putString("mqtt",Gson().toJson(mqttlist)).commit()
                    adapter.notifyDataSetChanged()
                }else{
                    Toast.makeText(this,"输入信息不能有空",Toast.LENGTH_LONG).show()
                }
                dialog.dismiss()
            }
            val layoutParams: ViewGroup.LayoutParams = view.layoutParams
            layoutParams.width = resources.displayMetrics.widthPixels
            view.layoutParams = layoutParams
            dialog.window.setGravity(Gravity.CENTER)
            dialog.show()

        }
    }
    fun initData(){
        val gson = Gson()
        val dataString: String = getSharedPreferences("mqtt_config",Context.MODE_PRIVATE).getString("mqtt","")
        if(!TextUtils.isEmpty(dataString)) {
            val data: MqttConfigList? = gson.fromJson<MqttConfigList>(dataString, MqttConfigList::class.java)
            if (data != null && data.datalist.size > 0) {
                list.addAll(data.datalist)
            }
        }
    }
    inner class RecycleAdapter(ctx: Context,list: ArrayList<MqttConfig>) : RecyclerView.Adapter<RecycleAdapter.MyViewHolder>(){
        var context = ctx
        var datalist = list
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.txtTitle.text=datalist[position].mqtt_client_id
            holder.txtHost.text=datalist[position].mqtt_host
            holder.txtPort.text=datalist[position].mqtt_port
            holder.txtName.text=datalist[position].user_name
            holder.txtPwd.text=datalist[position].pass_word
            holder.txtTopic.text=datalist[position].mqtt_topic
            holder.btnConnect.setOnClickListener {
                gotoPushActivity(datalist[position])
            }
            holder.btnDelete.setOnClickListener {
                deleteSingleConfig(datalist[position])
            }
        }
        fun gotoPushActivity(mqttConfig: MqttConfig){
            val intent = Intent()
            intent.setClass(context,PushActivity::class.java)
            intent.putExtra("MQTT_CONFIG",mqttConfig)
            Log.d("sssss",mqttConfig.toString())
            context.startActivity(intent)
        }
        fun deleteSingleConfig(mqttConfig: MqttConfig){
            for (mqttconfig: MqttConfig in datalist){
                if(mqttconfig===mqttConfig){
                    datalist.remove(mqttconfig)
                    MainActivity@adapter.notifyDataSetChanged()
                    val mqttlist: MqttConfigList = MqttConfigList(list)
                    getSharedPreferences("mqtt_config",Context.MODE_PRIVATE).edit().putString("mqtt",Gson().toJson(mqttlist)).apply()
                }
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyViewHolder{
            val view: View = LayoutInflater.from(context).inflate(R.layout.layout_config_item,parent,false)
            val holder = MyViewHolder(view)
            return holder
        }

        override fun getItemCount(): Int {
            return datalist.size
        }
        inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var txtTitle = view.findViewById<TextView>(R.id.txt_title)
            var txtHost = view.findViewById<TextView>(R.id.txt_host)
            var txtPort = view.findViewById<TextView>(R.id.txt_port)
            var txtName = view.findViewById<TextView>(R.id.txt_user_name)
            var txtPwd = view.findViewById<TextView>(R.id.txt_pwd)
            var txtTopic = view.findViewById<TextView>(R.id.txt_topic)
            var btnConnect = view.findViewById<Button>(R.id.btn_connect)
            var btnDelete = view.findViewById<Button>(R.id.btn_delete)
        }
    }
}
