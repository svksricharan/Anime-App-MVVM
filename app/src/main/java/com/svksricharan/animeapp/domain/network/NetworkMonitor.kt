package com.svksricharan.animeapp.domain.network

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
    fun isCurrentlyConnected(): Boolean
    fun observeConnectivity(): Flow<Boolean>
}
