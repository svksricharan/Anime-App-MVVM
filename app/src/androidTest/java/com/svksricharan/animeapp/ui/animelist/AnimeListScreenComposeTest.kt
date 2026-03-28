package com.svksricharan.animeapp.ui.animelist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.svksricharan.animeapp.domain.usecase.GetTopAnimePageUseCase
import com.svksricharan.animeapp.domain.usecase.ObserveNetworkConnectivityUseCase
import com.svksricharan.animeapp.fakes.FakeAnimeRepository
import com.svksricharan.animeapp.fakes.FakeNetworkMonitor
import com.svksricharan.animeapp.support.TestAnimeFactory
import com.svksricharan.animeapp.ui.theme.AnimeAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AnimeListScreenComposeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun displaysToolbarAndAnimeTitle() {
        val anime = TestAnimeFactory.anime(title = "Spy x Family")
        val repository = FakeAnimeRepository(firstPageAnime = listOf(anime))
        val viewModel = AnimeListViewModel(
            GetTopAnimePageUseCase(repository),
            ObserveNetworkConnectivityUseCase(FakeNetworkMonitor())
        )

        composeRule.setContent {
            AnimeAppTheme {
                AnimeListScreen(
                    viewModel = viewModel,
                    showImages = false,
                    onToggleImages = {},
                    onAnimeClick = {}
                )
            }
        }

        composeRule.onNodeWithText("Top Anime").assertIsDisplayed()
        composeRule.onNodeWithText("Spy x Family").assertIsDisplayed()
    }
}
