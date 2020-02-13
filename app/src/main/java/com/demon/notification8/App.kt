package com.demon.notification8

import android.app.Application
import android.content.Context


/**
 * @author DeMon
 * Created on 2020/2/10.
 * E-mail 757454343@qq.com
 * Desc:
 */
class App : Application() {
    private val TAG = this.javaClass.simpleName

    companion object {
        lateinit var application: Application
        lateinit var appContext: Context
    }


    override fun onCreate() {
        super.onCreate()
        application = this
        appContext = applicationContext

        NotificationHelper.Instance
    }
}