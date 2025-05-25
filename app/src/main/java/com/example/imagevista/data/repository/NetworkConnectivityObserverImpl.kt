package com.example.imagevista.data.repository

import android.app.DownloadManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.imagevista.domain.model.NetworkStatus
import com.example.imagevista.domain.repository.NetworkConnectivityObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn

// 1. Observes the network connection
// 2. Gives access to the system service (context) & lifetime (scope)
// 3. Knows when to start or stop
class NetworkConnectivityObserverImpl(
    context: Context,               // To get access to system services, like network services.
    scope: CoroutineScope           // A coroutine in which the stateflow will be active
): NetworkConnectivityObserver {

    // 1. Provides info about device's current network state
    // 2. Allows registering network callbacks
    // 3. Checks if the device is connected to Wi-Fi / mobile data
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    
    private val _networkStatus = observe().stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = NetworkStatus.Disconnected
    )

    override val networkStatus: StateFlow<NetworkStatus> = _networkStatus

    private fun observe(): Flow<NetworkStatus> {
        return callbackFlow {
            val connectivityCallback = object : NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    trySend(NetworkStatus.Connected)
                }

                override fun onLost(network: Network) {
                    super.onAvailable(network)
                    trySend(NetworkStatus.Disconnected)

                }
            }
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build()
            connectivityManager.registerNetworkCallback(request, connectivityCallback)
            awaitClose{
                connectivityManager.unregisterNetworkCallback(connectivityCallback)
            }
        }
            .distinctUntilChanged()
    }
}