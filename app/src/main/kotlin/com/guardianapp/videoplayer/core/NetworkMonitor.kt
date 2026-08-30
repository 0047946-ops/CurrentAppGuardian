package com.guardianapp.videoplayer.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NetworkState(
    val isConnected: Boolean = false,
    val isWiFi: Boolean = false,
    val isMobile: Boolean = false,
    val estimatedBandwidthMbps: Float = 0f,
    val networkType: String = "Unknown"
)

class NetworkMonitor(private val context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _networkState = MutableStateFlow(NetworkState())
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: android.net.Network) {
            updateNetworkState()
        }

        override fun onLost(network: android.net.Network) {
            updateNetworkState()
        }

        override fun onCapabilitiesChanged(
            network: android.net.Network,
            capabilities: NetworkCapabilities
        ) {
            updateNetworkState()
        }
    }

    fun startMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        updateNetworkState()
    }

    fun stopMonitoring() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun updateNetworkState() {
        val network = connectivityManager.activeNetwork ?: return
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return

        val isWiFi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val isMobile = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        val isConnected = isWiFi || isMobile

        val networkType = when {
            isWiFi -> "WiFi"
            isMobile -> "Mobile"
            else -> "Unknown"
        }

        // Estimate bandwidth
        val estimatedBandwidth = capabilities.linkDownstreamBandwidthKbps.toFloat() / 1000f

        _networkState.value = NetworkState(
            isConnected = isConnected,
            isWiFi = isWiFi,
            isMobile = isMobile,
            estimatedBandwidthMbps = estimatedBandwidth,
            networkType = networkType
        )
    }

    fun isConnected(): Boolean = _networkState.value.isConnected
    fun isWiFi(): Boolean = _networkState.value.isWiFi
    fun isMobile(): Boolean = _networkState.value.isMobile
    fun getEstimatedBandwidthMbps(): Float = _networkState.value.estimatedBandwidthMbps
}
