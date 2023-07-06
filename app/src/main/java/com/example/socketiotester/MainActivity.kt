package com.example.socketiotester

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.socketiotester.network.EasySocketIo
import com.example.socketiotester.ui.theme.SocketioTesterTheme
import io.socket.client.Socket
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    private var msocket : Socket?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SocketioTesterTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting("Android")
                    SocketChatScreen()
                }
            }
        }

    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SocketioTesterTheme {
        Greeting("Android")
    }
}

@Composable
fun SocketChatScreen() {
    val easySocketIO = remember { EasySocketIo() }
    val chatMessages = remember { mutableStateListOf<String>() }
    var messageInput by remember { mutableStateOf("") }
    var connectionError by remember { mutableStateOf<String?>(null) }
    val url = "http://3.108.221.34:2000/"

    LaunchedEffect(Unit) {
        try {
            easySocketIO.connect(url){
                error -> connectionError = error
            }

            // Listen for 'bot_reply' event from the server and update chatMessages
            easySocketIO.onEvent("bot_reply") { data ->
                val message = data.optString("message")
                chatMessages.add("Bot: $message")
                Log.d("ResponseStructure", data.toString(4))
            }

            // Listen for 'disconnect' event from the server and update chatMessages
            easySocketIO.onEvent(Socket.EVENT_DISCONNECT) {
                chatMessages.add("Disconnected from the server")
            }
        } catch (e: Exception) {
            connectionError = e.message
        }
    }

    if (connectionError != null) {
        Text("Connection Error: $connectionError")
        return
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Chat Messages")
        for (chatMessage in chatMessages) {
            Text(chatMessage)
        }
        if (easySocketIO.isConnected()) {
            Text("Connected")
        } else {
            Text(text = "Not COnnected")
        }

        TextField(
            value = messageInput,
            onValueChange = { messageInput = it },
            label = { Text("Enter your message") }
        )

        Button(onClick = {
            val message = messageInput.trim()
            if (message.isNotBlank()) {
                val jsonObject = JSONObject()
                try {
                    jsonObject.put("message", message)
                    jsonObject.put("userId", "456")
                    jsonObject.put("OrganisationId", 43)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                GlobalScope.launch {
                    easySocketIO.sendEvent("user_reply", jsonObject)
                }
                messageInput = ""



            }
        }) {
            Text("Send")
        }
    }
}



