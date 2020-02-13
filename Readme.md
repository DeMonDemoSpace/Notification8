### Android8.0通知栏

### 渠道
1. 8.0系统后使用通知栏需要先创建渠道，多渠道可以设置渠道组进行管理。  
2. 如果你项目的targetSdkVersion>=26，如果没有进行渠道适配，将完全无法弹出通知栏。  
3. 如果targetSdkVersion>=26，且升级了Android核心库，那么原来的通知栏构造方法会显示为废弃方法，需要多传一个渠道Id，如果该渠道Id没有提前创建，否则会异常。

![在这里插入图片描述](https://img-blog.csdnimg.cn/2020021221431438.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L0RlTW9ubGl1aHVp,size_16,color_FFFFFF,t_70)

#### 创建通知栏渠道组
1. 两个参数：渠道组id，渠道组在通知栏管理里面显示的名字name。
2. 创建了渠道组后，8.0系统通知栏管理渠道会分组显示。
3. 渠道组除了在通知栏里面会分组显示外，暂时没有发现有其他额外的功能，也可以不创建。

```kotlin
@TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannelGroup(id: String, name: String) {
      val manager: NotificationManager = App.appContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannelGroup(id, name)
        manager.createNotificationChannelGroup(channel)
    }
```

#### 创建通知栏渠道

1. 三个个参数：渠道id，渠道在通知栏管理里面显示的名字name，渠道重要等级详情见下文。
2. 可以通过```channel.group = groupId```设置该渠道所属的渠道组，但是需要先创建渠道组，否则会异常。
3. 渠道也可以不设置渠道组，不会有任何影响。不设置的渠道会统一系统默认的渠道组下。

```kotlin
 @TargetApi(Build.VERSION_CODES.O)
     private fun createNotificationChannel(id: String, name: String, importance: Int, groupId: String = "") {
     val manager: NotificationManager = App.appContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
         val channel = NotificationChannel(id, name, importance)
         if (groupId.isNotEmpty()) {
             channel.group = groupId //设置该渠道所属的渠道组
         }
         manager.createNotificationChannel(channel)
     }
```

#### 渠道重要等级
1. 8.0以前通知栏的提示音，提示音，振动，呼吸灯等效果使用方法setDefaults()，setSound()，setVibrate()等方法设置的；悬浮框,是否折叠等效果则是由setPriority()设置优先级函数来控制的；
但是这些函数在8.0以后会失效，而是使用由渠道重要等级来代替。
2. 8.0以前通知栏需要setPriority()优先级>=PRIORITY_DEFAULT，并且设置了setDefaults()，setSound()，setVibrate()等方法设置才会有提示音。
3. 8.0以后，需要合理设置渠道重要等级，合理划分，不重要的通知应设置低等级，避免频繁骚扰而被关闭。
4. 渠道重要等级的提示效果只在8.0以上起作用，为了兼容低版本系统，最好是合理设置渠道重要等级的同时使用setDefaults()&setPriority()等函数设置提示效果。

|渠道重要等级|值|说明|
|:--|:--|:--|
|NotificationManager.IMPORTANCE_NONE|0|通知栏完全不显示，没有提示。也是通知栏管理设置为关闭时的状态。|
|NotificationManager.IMPORTANCE_MIN|1|通知栏会被折叠显示，没有提示。|
|NotificationManager.IMPORTANCE_LOW|2|通知栏正常显示，但是没有提示。|
|NotificationManager.IMPORTANCE_DEFAULT|3|通知栏正常显示,有提示。|
|NotificationManager.IMPORTANCE_HIGH|4|通知栏正常显示,提示，屏幕上方还会有悬浮弹框。|
|NotificationManager.IMPORTANCE_MAX|5|源码注释：Unused，没用。最大等级，测试效果上与IMPORTANCE_HIGH完全一致。|


#### 删除渠道组及渠道

```kotlin
 val manager: NotificationManager = App.appContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
  manager.deleteNotificationChannel(channelId) //删除渠道
            manager.deleteNotificationChannelGroup(groupId)  //删除渠道组
```
 1. 我们可以通过上述两个方法删除渠道和渠道组。
 2. 但是这个功能非常不建议大家使用。因为Google为了防止应用程序随意地创建垃圾通知渠道，会在通知设置界面显示所有被删除的通知渠道数量，
 3. 所以对于开发者来说最好的做法就是仔细规划好通知渠道，而不要轻易地使用删除功能。
   

### 通知栏权限

>Api 24以上，NotificationManager提供了 areNotificationsEnabled()方法检测通知权限。
 support包已经考虑了以上场景，在 24.1.0 开放了areNotificationsEnabled()，在19以下默认返回true,19-24返回对应反射值，24以上用原生NotificationManager检测。

#### areNotificationsEnabled源码
```java
public final class NotificationManagerCompat {
 //...
public boolean areNotificationsEnabled() {
        if (Build.VERSION.SDK_INT >= 24) {
            return mNotificationManager.areNotificationsEnabled();
        } else if (Build.VERSION.SDK_INT >= 19) {
            AppOpsManager appOps =
                    (AppOpsManager) mContext.getSystemService(Context.APP_OPS_SERVICE);
            ApplicationInfo appInfo = mContext.getApplicationInfo();
            String pkg = mContext.getApplicationContext().getPackageName();
            int uid = appInfo.uid;
            try {
                Class<?> appOpsClass = Class.forName(AppOpsManager.class.getName());
                Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE,
                        Integer.TYPE, String.class);
                Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
                int value = (int) opPostNotificationValue.get(Integer.class);
                return ((int) checkOpNoThrowMethod.invoke(appOps, value, uid, pkg)
                        == AppOpsManager.MODE_ALLOWED);
            } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException
                    | InvocationTargetException | IllegalAccessException | RuntimeException e) {
                return true;
            }
        } else {
            return true;
        }
    }
    //...
}
```


1. 使用```NotificationManagerCompat.from(App.appContext).areNotificationsEnabled()```方法即可获得应用是否被关闭通知栏。

2.8.0以上只有关闭应用通知栏才会返回false，关闭某一个渠道并不会改变该函数的返回值。
3.获取某一渠道是否被关闭，可以通过判断渠道重要等级来判断,如下代码。
```kotlin
 @RequiresApi(Build.VERSION_CODES.O)
    private fun isNotificationEnabledV26(channelId: String): Boolean {
        runCatching {
             val manager: NotificationManager = App.appContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = manager.getNotificationChannel(channelId)
            return channel.importance != NotificationManager.IMPORTANCE_NONE
        }.onFailure {
            it.printStackTrace()
        }
        return true
    }
```

### 应用内打开通知栏管理界面

#### 打开应用的通知栏权限设置界面

```kotlin
fun openNotification(){
     val pkg: String = App.appContext.packageName
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
}
```

#### 8.0打开某一渠道的通知栏权限设置界面

```kotlin
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
```


### 通知栏

#### 完整的创建通知栏的代码
**通知栏通知函数NotificationManager.notify(int id, Notification notification),id必须是唯一的，否则只会显示最新的通知。**

```kotlin
        //点击通知栏消息跳转页
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val manager: NotificationManager = App.appContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
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
                 /* .setGroup(NotificationHelper.CHAT)
                     .setGroupSummary(true)*/
                .build()
            manager.notify(0, notification)
```

#### 常用方法:
setContentTitle(CharSequence)：设置标题   
setContentText(CharSequence)：设置内容  
setSubText(CharSequence)：设置内容下面一小行的文字  
setTicker(CharSequence)：设置收到通知时在顶部显示的文字信息  
setWhen(long)：设置通知时间，一般设置的是收到通知时的System.currentTimeMillis()  
setSmallIcon(int)：设置右下角的小图标，在接收到通知的时候顶部也会显示这个小图标  
setLargeIcon(Bitmap)：设置左边的大图标  
setAutoCancel(boolean)：用户点击Notification点击面板后是否让通知取消(默认不取消)  

#### 8.0以下设置通知提示
>setDefaults(int)：向通知添加声音、闪灯和振动效果的最简单、使用默认（defaults）属性，可以组合多个属性。
>setDefaults(Notification.DEFAULT_SOUND) //获取默认铃声
>Notification.DEFAULT_VIBRATE(添加默认震动提醒)；
>Notification.DEFAULT_SOUND(添加默认声音提醒)；
>Notification.DEFAULT_LIGHTS(添加默认三色灯/呼吸灯提醒)
>Notification.DEFAULT_ALL(添加默认以上3种全部提醒)
>
>setVibrate(long[])：设置振动方式，比如：setVibrate(new long[] {0,300,500,700});延迟0ms，然后振动300ms，在延迟500ms，接着再振动700ms。
>setLights(int argb, int onMs, int offMs)：设置三色灯，参数依次是：灯光颜色，亮持续时间，暗的时间，不是所有颜色都可以，这跟设备有关，有些手机还不带三色灯；
另外，还需要为Notification设置flags为Notification.FLAG_SHOW_LIGHTS才支持三色灯提醒！
>
>setSound(Uri)：设置接收到通知时的铃声，可以用系统的，也可以自己设置，例子如下:
>setSound(Uri.parse("file:///sdcard/xx/xx.mp3")) //获取自定义铃声
>setSound(Uri.withAppendedPath(Audio.Media.INTERNAL_CONTENT_URI, "5"))//获取Android多媒体库内的铃声

**8.0以前通知栏需要setPriority()优先级>=PRIORITY_DEFAULT，并且设置了setDefaults()，setSound()，setVibrate()等方法设置才会有提示音。**


#### 8.0一下设置优先级
>setPriority(int)：优先级决定是否显示顶部悬浮框，是否状态栏显示图标

**8.0以前通知栏需要setPriority()优先级>=PRIORITY_DEFAULT，并且设置了setDefaults()，setSound()，setVibrate()等方法设置才会有提示音。**

|优先级|说明|
|:--|:--|
|NotificationCompat.PRIORITY_MAX|重要而紧急的通知，通知用户这个事件是时间上紧迫的或者需要立即处理的。|
|NotificationCompat.PRIORITY_HIGH|高优先级用于重要的通信内容，例如短消息或者聊天，这些都是对用户来说比较有兴趣的。|
|NotificationCompat.PRIORITY_DEFAULT|默认优先级用于没有特殊优先级分类的通知。|
|NotificationCompat.PRIORITY_LOW|低优先级可以通知用户但又不是很紧急的事件。|
|NotificationCompat.PRIORITY_MIN|用于后台消息 (例如天气或者位置信息)。最低优先级通知将只在状态栏显示图标，只有用户下拉通知抽屉才能看到内容。|

#### 进度条通知栏
>setProgress(int,int,boolean)：设置带进度条的通知参数依次为：进度条最大数值，当前进度，进度是否不确定；
如果为确定的进度条：调用setProgress(max, progress, false)来设置通知，在更新进度的时候在此发起通知更新progress，并且在下载完成后要移除进度条cancel(int id)。
如果为不确定（持续活动）的进度条，这是在处理进度无法准确获知时显示活动正在持续，所以调用setProgress(0, 0, true)。


```kotlin
 val manager: NotificationManager = App.appContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
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
            //倒计时模拟下载进度
            object : CountDownTimer(10000, 100) {
                override fun onFinish() {
                   manager.cancel(3)
                }

                override fun onTick(millisUntilFinished: Long) {
                    builder.setProgress(100, (100 - millisUntilFinished / 100).toInt(), false)
                    manager.notify(3, builder.build())
                }

            }.start()
```

#### 点击通知栏跳转
>setContentIntent(PendingIntent)：设置该方法点击通知栏可以跳转到指定的PendingIntent。
PendingIntent和Intent略有不同，它可以设置执行次数，主要用于远程服务通信、闹铃、通知、启动器、短信中，在一般情况下用的比较少。
比如这里通过Pending启动Activity：getActivity(Context, int, Intent, int)，当然还可以启动Service或者BroadcastPendingIntent的位标识符(第四个参数)：
FLAG_ONE_SHOT 表示返回的PendingIntent仅能执行一次，执行完后自动取消
FLAG_NO_CREATE 表示如果描述的PendingIntent不存在，并不创建相应的PendingIntent，而是返回NULL
FLAG_CANCEL_CURRENT 表示相应的PendingIntent已经存在，则取消前者，然后创建新的PendingIntent，这个有利于数据保持为最新的，可以用于即时通信的通信场景
FLAG_UPDATE_CURRENT 表示更新的PendingIntent



### 参考
[Android通知栏微技巧，8.0系统中通知栏的适配](https://blog.csdn.net/guolin_blog/article/details/79854070)  
[Android开发中Notification通知栏的基本用法（总结）](https://blog.csdn.net/lpcrazyboy/article/details/80756817)








