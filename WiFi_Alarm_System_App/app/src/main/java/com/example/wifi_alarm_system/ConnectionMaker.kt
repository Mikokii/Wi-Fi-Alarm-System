package com.example.wifi_alarm_system

import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck
import java.util.UUID


class ConnectionMaker {
    private val client: Mqtt5Client = Mqtt5Client.builder()
        .identifier(UUID.randomUUID().toString())
        .serverHost("")
        .serverPort(8883)
        .sslWithDefaultConfig()
        .simpleAuth()
        .username("")
        .password("".toByteArray())
        .applySimpleAuth()
        .build()

    fun connectAndSubscribe(onMessageReceived: (String) -> Unit): String{
        return try {
            val connAckMessage: Mqtt5ConnAck = client.toBlocking().connect()
            client.toAsync().subscribeWith()
                .topicFilter("movement")
                .callback { publish: Mqtt5Publish ->
                    val message = String(publish.payloadAsBytes)
                    onMessageReceived(message)
                }
                .send()
            connAckMessage.reasonCode.toString()
        } catch (e: Exception){
            e.message ?: "Unknown error"
        }
    }
}