package com.svksricharan.animeapp.domain.usecase

import com.svksricharan.animeapp.domain.network.NetworkMonitor
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class ObserveNetworkConnectivityUseCaseTest {

    private val networkMonitor: NetworkMonitor = mock()
    private val useCase = ObserveNetworkConnectivityUseCase(networkMonitor)

    @Test
    fun invoke_delegatesToNetworkMonitor() = runTest {
        `when`(networkMonitor.observeConnectivity()).thenReturn(flowOf(true, false))

        val emitted = mutableListOf<Boolean>()
        useCase().collect { emitted += it }

        verify(networkMonitor).observeConnectivity()
        assertEquals(listOf(true, false), emitted)
    }
}
