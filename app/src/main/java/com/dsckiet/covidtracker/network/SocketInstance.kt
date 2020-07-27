package com.dsckiet.covid_project_demo

import android.app.Application
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import java.net.URISyntaxException

/**
 * Created by Vidit on 24-07-2020
 */

private const val URL = "https://covid-project-gzb.herokuapp.com"

class SocketInstance : Application() {
    //socket.io connection url

    private var mSocket: Socket? = null

    override fun onCreate() {
        super.onCreate()
        try {
//creating socket instance
            mSocket = IO.socket(URL)
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
    }

    //return socket instance
    fun getMSocket(): Socket? {
        return mSocket
    }
}