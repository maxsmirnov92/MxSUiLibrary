package net.maxsmr.testapp

import android.app.Application
import net.maxsmr.commonutils.logger.BaseLogger
import net.maxsmr.commonutils.logger.LogcatLogger
import net.maxsmr.commonutils.logger.holder.BaseLoggerHolder

class TestApp : Application() {

    override fun onCreate() {

        BaseLoggerHolder.initInstance {
            object : BaseLoggerHolder(false) {
                override fun createLogger(className: String): BaseLogger {
                    return LogcatLogger(className)
                }
            }
        }

        super.onCreate()
    }
}