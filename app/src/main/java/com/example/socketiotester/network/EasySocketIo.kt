package com.example.socketiotester.network


/*
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URISyntaxException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class EasySocketIo {
    private var socket: Socket? = null

    suspend fun connect(socketUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val options = IO.Options()
            options.transports = arrayOf(io.socket.engineio.client.transports.WebSocket.NAME)

            socket = IO.socket(socketUrl, options)

            val connectResult = suspendCoroutine<Boolean> { continuation ->
                socket?.on(Socket.EVENT_CONNECT) {
                    continuation.resume(true)
                }
                socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                    continuation.resume(false)
                }

                socket?.connect()
            }
            if (connectResult) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Socket connection failed"))
            }
        } catch (e: URISyntaxException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun disconnect() {
        socket?.off()
        socket?.disconnect()
    }

    fun isConnected(): Boolean {
        return socket?.connected() ?: false
    }

   *//* suspend fun sendEvent(eventName: String, data: Any?) = suspendCoroutine<Unit> { continuation ->
        try {
            socket?.emit(eventName, data, Ack { args ->
                val exception = args.getOrNull(0) as? Exception
                if (exception != null) {
                    continuation.resumeWithException(exception)
                } else {
                    continuation.resume(Unit)
                }
            })
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }*//*
   suspend fun sendEvent(eventName: String, vararg data: Any?) = suspendCoroutine<Unit> { continuation ->
       socket?.emit(eventName, data) { args ->
           // Handle the acknowledgement if needed
           // For simplicity, we're not using it in this example
           continuation.resume(Unit)
       }
   }


    fun onEvent(eventName: String, listener: (JSONObject) -> Unit) {
        socket?.on(eventName, object : Emitter.Listener {
            override fun call(vararg args: Any) {
                val data = args.getOrNull(0) as? JSONObject
                if (data != null) {
                    listener(data)
                }
            }
        })
    }
}*/
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.net.URISyntaxException

class EasySocketIo {
    private var socket: Socket? = null

    fun connect(socketUrl: String, onError: (String) -> Unit) {
        try {
            val options = IO.Options()
            options.transports = arrayOf(io.socket.engineio.client.transports.WebSocket.NAME)
            socket = IO.socket(socketUrl, options)

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val errorMessage = "Socket connection error: ${args?.getOrNull(0)}"
                onError(errorMessage)
            }
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }

        socket?.connect()
    }

    fun disconnect() {
        socket?.disconnect()
    }

    fun isConnected(): Boolean {
        return socket?.connected() ?: false
    }

    fun sendEvent(eventName: String, data: Any?) {
        socket?.emit(eventName, data)
    }

    fun onEvent(eventName: String, listener: (JSONObject) -> Unit) {
        socket?.on(eventName, object : Emitter.Listener {
            override fun call(vararg args: Any?) {
                val data = args[0] as JSONObject
                listener(data)
            }
        })
    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.M)
    fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

