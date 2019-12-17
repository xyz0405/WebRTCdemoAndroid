package com.tuan88291.mvvmpattern.view.activity.videocall

import android.os.Build
import androidx.lifecycle.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.tuan88291.mvvmpattern.data.local.model.DataCall
import com.tuan88291.mvvmpattern.utils.observe.AutoDisposable
import com.tuan88291.mvvmpattern.utils.observe.ObserveEasy
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

class SocketClient(private val mSocket: Socket): LifecycleObserver {
    val gson = Gson()
    var callbacks: SignallingClientListener? = null
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreateSocket() {
        mSocket.on("Received", onReceived)
        mSocket.on("onAnswerAccept", onAnswerAccept)
        mSocket.on("inComing", onInComing)
    }
    fun setCallback(callback: SignallingClientListener) {
        this.callbacks = callback
    }
    fun onCallVideo(data: Any?) {
        val dataSt = DataCall(Build.MODEL, gson.toJson(data))
        mSocket.emit("call", gson.toJson(dataSt))
    }
    fun onStartCall() {
        mSocket.emit("startCall", Build.MODEL)
    }
    fun onStartAnswer() {
        mSocket.emit("startAnswer", Build.MODEL)
    }
    private val onInComing = object : Emitter.Listener {

        override fun call(vararg args: Any?) {

            object : ObserveEasy() {
                override fun getDispose(): AutoDisposable? {
                    return null
                }

                override fun doBackground(): Any? {
                    return null
                }

                override fun onSuccess(result: Any?) {
                    super.onSuccess(result)
                    callbacks?.onInComing()
                }

            }
        }
    }
    private val onAnswerAccept = object : Emitter.Listener {

        override fun call(vararg args: Any?) {
            object : ObserveEasy() {
                override fun getDispose(): AutoDisposable? {
                    return null
                }

                override fun doBackground(): Any? {
                    return null
                }

                override fun onSuccess(result: Any?) {
                    super.onSuccess(result)
                    callbacks?.onAnswerAccept()
                }

            }

        }
    }
    private val onReceived = object : Emitter.Listener {

        override fun call(vararg args: Any?) {
            object : ObserveEasy() {
                override fun getDispose(): AutoDisposable? {
                    return null
                }
                override fun doBackground(): Any? {
                    val jsonObject = gson.fromJson(args[0].toString(), JsonObject::class.java)
                    return jsonObject
                }
                override fun onSuccess(result: Any?) {
                    val jsonObject = result as JsonObject
                    if (jsonObject.has("serverUrl")) {
                        callbacks?.onIceCandidateReceived(gson.fromJson(jsonObject, IceCandidate::class.java))
                    } else if (jsonObject.has("type") && jsonObject.get("type").asString == "OFFER") {
                        callbacks?.onOfferReceived(gson.fromJson(jsonObject, SessionDescription::class.java))
                    } else if (jsonObject.has("type") && jsonObject.get("type").asString == "ANSWER") {
                        callbacks?.onAnswerReceived(gson.fromJson(jsonObject, SessionDescription::class.java))
                    }
                }
            }

        }
    }
//    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
//    fun onClear() {
//        mSocket.emit("clearUser", Build.MODEL)
//    }
}