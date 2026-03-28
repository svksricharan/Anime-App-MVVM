package com.svksricharan.animeapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.svksricharan.animeapp.BuildConfig
import com.svksricharan.animeapp.di.AppContainer
import com.svksricharan.animeapp.ui.animedetail.AnimeDetailScreen
import com.svksricharan.animeapp.ui.animedetail.AnimeDetailViewModel
import com.svksricharan.animeapp.ui.animelist.AnimeListScreen
import com.svksricharan.animeapp.ui.animelist.AnimeListViewModel

sealed class Screen(val route: String) {
    object AnimeList : Screen("anime_list")
    object AnimeDetail : Screen("anime_detail/{animeId}") {
        fun createRoute(animeId: Int) = "anime_detail/$animeId"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    appContainer: AppContainer
) {
    var showImages by rememberSaveable { mutableStateOf(BuildConfig.SHOW_IMAGES) }

    NavHost(
        navController = navController,
        startDestination = Screen.AnimeList.route
    ) {
        composable(Screen.AnimeList.route) {
            val viewModel: AnimeListViewModel = viewModel(
                factory = AnimeListViewModel.Factory(
                    getTopAnimePageUseCase = appContainer.getTopAnimePageUseCase,
                    observeNetworkConnectivityUseCase = appContainer.observeNetworkConnectivityUseCase
                )
            )
            AnimeListScreen(
                viewModel = viewModel,
                showImages = showImages,
                onToggleImages = { showImages = !showImages },
                onAnimeClick = { animeId ->
                    navController.navigate(Screen.AnimeDetail.createRoute(animeId)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.AnimeDetail.route,
            arguments = listOf(
                navArgument("animeId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val animeId = backStackEntry.arguments?.getInt("animeId") ?: return@composable
            val viewModel: AnimeDetailViewModel = viewModel(
                factory = AnimeDetailViewModel.Factory(
                    getAnimeDetailUseCase = appContainer.getAnimeDetailUseCase,
                    animeId = animeId
                )
            )
            AnimeDetailScreen(
                viewModel = viewModel,
                showImages = showImages,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
