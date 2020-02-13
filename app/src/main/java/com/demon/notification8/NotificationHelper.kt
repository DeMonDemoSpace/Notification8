package com.demon.notification8

import android.annotation.TargetApi
import android.app.*
import android.app.Notification.AUDIO_ATTRIBUTES_DEFAULT
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import java.lang.reflect.Field
import java.lang.reflect.Method


/**
 * @author DeMon
 * Created on 2020/2/10.
 * E-mail 757454343@qq.com
 * Desc:
 */
class NotificationHelper {

    val manager: NotificationManager = App.appContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager


    companion object {
        val NORMAl = "Normal" //普通组
        val LIKE = "Like" //点赞
        val DOWNLOAD = "Download" //下载


        val CHAT = "Chat" //聊天组
        val VIDEO = "Video" //视频
        val VOICE = "Voice" //语音

        val NOTICE = "Notice" //通知组
        val SUBSCRIBE = "Subscribe" //订阅

        val Instance = Helper.instance
    }

    private object Helper {
        val instance = NotificationHelper()
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannelGroup(CHAT, "聊天消息")
            createNotificationChannel(VIDEO, "视频", NotificationManager.IMPORTANCE_MAX, CHAT)
            createNotificationChannel(VOICE, "语音", NotificationManager.IMPORTANCE_HIGH, CHAT)

            createNotificationChannelGroup(NOTICE, "通知消息")
            createNotificationChannel(SUBSCRIBE, "订阅", NotificationManager.IMPORTANCE_DEFAULT, NOTICE)

            createNotificationChannelGroup(NORMAl, "普通消息")
            createNotificationChannel(DOWNLOAD, "下载", NotificationManager.IMPORTANCE_LOW, NORMAl)
            createNotificationChannel(LIKE, "点赞", NotificationManager.IMPORTANCE_MIN, NORMAl)
        }

    }


    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(id: String, name: String, importance: Int, groupId: String = "") {
        val channel = NotificationChannel(id, name, importance)
        if (groupId.isNotEmpty()) {
            channel.group = groupId
        }
        /*if (importance >= NotificationManager.IMPORTANCE_DEFAULT) {
            //声音
            channel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, AUDIO_ATTRIBUTES_DEFAULT)
        }

        if (importance >= NotificationManager.IMPORTANCE_HIGH) {
            //振动
            channel.enableVibration(true)
            //应用角标
            channel.setShowBadge(true)
            //呼吸灯
            channel.enableLights(true)
            //锁屏显示
            channel.lockscreenVisibility = 0
        }*/
        manager.createNotificationChannel(channel)
    }


    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannelGroup(id: String, name: String) {
        val channel = NotificationChannelGroup(id, name)
        manager.createNotificationChannelGroup(channel)
    }

    /**
     * 检查应用的通知栏权限
     *
     * @param channelId 渠道id
     * channelId为空则检查应用的通知权限，不为空则检查对应渠道的通知权限
     */
    fun isNotificationEnabled(channelId: String = ""): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && channelId.isNotEmpty()) {
            isNotificationEnabledV26(channelId)
        } else {
            NotificationManagerCompat.from(App.appContext).areNotificationsEnabled()
        }
    }

    /**
     * Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
     * 针对8.0及以上设备，判断渠道Id的通知栏是否被关闭
     *
     * @param channelId 渠道Id
     * @return
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun isNotificationEnabledV26(channelId: String): Boolean {
        runCatching {
            val channel = manager.getNotificationChannel(channelId)
            return channel.importance != NotificationManager.IMPORTANCE_NONE
        }.onFailure {
            it.printStackTrace()
        }
        return true
    }


    /**
     * 打开通知栏权限
     */
    fun openNotification(context: Context) {
        val pkg: String = App.appContext.packageName
        AlertDialog.Builder(context).setTitle("提示").setMessage("请设置应用允许通知，否则无法接收消息！")
            .setNegativeButton("算了") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("确定") { _, _ ->
                val intent = Intent()
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                        intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, pkg)
                    }
                    Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT -> {
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        intent.addCategory(Intent.CATEGORY_DEFAULT)
                        intent.data = Uri.parse("package:$pkg")
                    }
                    else -> {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        intent.data = Uri.fromParts("package", pkg, null)
                    }
                }
                App.appContext.startActivity(intent)
            }.show()
    }


    /**
     * 直接打开对应渠道Id的设置界面
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun openNotificationByChannelId(channelId: String) {
        val intent = Intent()
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.action = Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, App.appContext.packageName)
        intent.putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
        App.appContext.startActivity(intent)
    }


}