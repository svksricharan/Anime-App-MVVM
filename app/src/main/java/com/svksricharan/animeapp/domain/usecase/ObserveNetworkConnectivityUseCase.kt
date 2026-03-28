package com.svksricharan.animeapp.domain.usecase

import com.svksricharan.animeapp.domain.network.NetworkMonitor
import kotlinx.coroutines.flow.Flow

class ObserveNetworkConnectivityUseCase(
    private val networkMonitor: NetworkMonitor
) {
    operator fun invoke(): Flow<Boolean> = networkMonitor.observeConnectivity()
}
