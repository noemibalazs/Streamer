package com.noemi.streamer.networkconnection

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class ConnectionServiceImpl @Inject constructor(
    private val connectivityManager: ConnectivityManager,
    scope: CoroutineScope
) : ConnectionService {

    private val callback = NetworkCallback()

    private val networkState = MutableStateFlow(getDefaultCurrentNetwork())

    override val isConnected: StateFlow<Boolean> =
        networkState.map {
            it.isConnected()
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = networkState.value.isConnected()
        )

    override fun startListenNetworkState() {
        if (networkState.value.isListening) return

        networkState.update {
            it.copy(isListening = true)
        }

        connectivityManager.registerDefaultNetworkCallback(callback)
    }

    override fun stopListenNetworkState() {
        if (!networkState.value.isListening) return

        networkState.update {
            it.copy(isListening = false)
        }

        connectivityManager.unregisterNetworkCallback(callback)
    }

    private inner class NetworkCallback : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            networkState.update {
                it.copy(isAvailable = true)
            }
        }

        override fun onUnavailable() {
            super.onUnavailable()
            networkState.update {
                it.copy(isAvailable = false, networkCapabilities = null)
            }
        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
            super.onLosing(network, maxMsToLive)
            networkState.update {
                it.copy(isAvailable = false)
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            networkState.update {
                it.copy(isAvailable = false, networkCapabilities = null)
            }
        }

        override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
            super.onBlockedStatusChanged(network, blocked)
            networkState.update {
                it.copy(isBlocked = blocked)
            }
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            networkState.update {
                it.copy(networkCapabilities = networkCapabilities)
            }
        }
    }

    private fun getDefaultCurrentNetwork(): CurrentNetwork {
        return CurrentNetwork(
            isListening = false,
            isAvailable = false,
            isBlocked = false,
            networkCapabilities = null
        )
    }

    private data class CurrentNetwork(
        val isListening: Boolean,
        val networkCapabilities: NetworkCapabilities?,
        val isAvailable: Boolean,
        val isBlocked: Boolean
    )

    private fun CurrentNetwork.isConnected(): Boolean =
        isListening && isAvailable && !isBlocked && networkCapabilities?.hasValidCapability() ?: false

    private fun NetworkCapabilities.hasValidCapability(): Boolean = when {
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
                (hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        hasTransport(NetworkCapabilities.TRANSPORT_VPN) ||
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) -> true
        else -> false
    }
}