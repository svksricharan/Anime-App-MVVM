package com.svksricharan.animeapp.fakes

import com.svksricharan.animeapp.domain.network.NetworkMonitor
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeNetworkMonitor(
    private val connected: Boolean = true
) : NetworkMonitor {
    override fun isCurrentlyConnected(): Boolean = connected

    override fun observeConnectivity(): Flow<Boolean> = flow {
        emit(connected)
        awaitCancellation()
    }
}
