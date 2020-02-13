package com.demon.notification8

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private val TAG = this.javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //这里假设视频消息的通知栏需要提醒打开
        //8.0系统先手动关闭视频消息的通知栏，8.0以下关闭通知
        if (!NotificationHelper.Instance.isNotificationEnabled()) {
            NotificationHelper.Instance.openNotification(this)
        }


        //点击通知栏消息跳转页
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)


        btnVideo.setOnClickListener {
            val notification = NotificationCompat.Builder(this, NotificationHelper.VIDEO)
                .setContentTitle("视频消息")
                .setContentText("收到一条视频消息，等级MAX！！！")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_large))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build()
            NotificationHelper.Instance.manager.notify(0, notification)
        }


        btnVoice.setOnClickListener {
            val notification = NotificationCompat.Builder(this, NotificationHelper.VOICE)
                .setContentTitle("语音消息")
                .setContentText("收到一条语音消息，等级HIGH！！！")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_large))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
               /* .setGroup(NotificationHelper.CHAT)
                .setGroupSummary(true)*/
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
            NotificationHelper.Instance.manager.notify(Random.nextInt(1, 10), notification)
        }

        btnSubscribe.setOnClickListener {
            val notification = NotificationCompat.Builder(this, NotificationHelper.SUBSCRIBE)
                .setContentTitle("订阅消息")
                .setContentText("收到一条订阅消息，等级DEFAULT！！！")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_large))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            NotificationHelper.Instance.manager.notify(2, notification)
        }


        btnDownload.setOnClickListener {
            val builder = NotificationCompat.Builder(this, NotificationHelper.DOWNLOAD)
                .setContentTitle("下载消息")
                .setContentText("下载进度，等级LOW！！！")
                .setProgress(100, 0, false)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_large))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)//设置为低优先级，且不设置提示音，防止下载更新通知的时候不断发出提示音

            object : CountDownTimer(10000, 100) {
                override fun onFinish() {
                    NotificationHelper.Instance.manager.cancel(3)
                }

                override fun onTick(millisUntilFinished: Long) {
                    builder.setProgress(100, (100 - millisUntilFinished / 100).toInt(), false)
                    NotificationHelper.Instance.manager.notify(3, builder.build())
                }

            }.start()
        }


        btnLike.setOnClickListener {
            val notification = NotificationCompat.Builder(this, NotificationHelper.LIKE)
                .setContentTitle("点赞消息")
                .setContentText("收到一条点赞消息，等级MIN！！！")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_large))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build()
            NotificationHelper.Instance.manager.notify(4, notification)
        }

        //点击手机后，立即锁屏，3秒后是否有锁屏效果
        btnLock.setOnClickListener {
            Handler().postDelayed({
                val notification = NotificationCompat.Builder(this, NotificationHelper.VIDEO)
                    .setContentTitle("锁屏消息")
                    .setContentText("收到一条锁屏消息，等级MAX！！！")
                    .setShowWhen(true)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_large))
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .build()
                NotificationHelper.Instance.manager.notify(5, notification)
            }, 3000)

        }
    }
}
