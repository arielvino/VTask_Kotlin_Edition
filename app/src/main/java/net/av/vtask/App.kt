package net.av.vtask

import android.app.Application
import android.content.Context
import java.io.File

class App : Application() {
    companion object {
        lateinit var appContext: Context
        private lateinit var userNameFile: File
        var userName: String?
            get() {
                return try {
                    userNameFile.readText()
                } catch (e: Exception) {
                    null

                }
            }
            set(value) {
                value?.let { userNameFile.writeText(it) } ?: userNameFile.delete()
            }
    }

    override fun onCreate() {
        super.onCreate()

        appContext = this
        userNameFile =
            File("${appContext.filesDir.absolutePath}/currentUser")
    }
}