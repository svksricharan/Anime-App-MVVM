package com.svksricharan.animeapp.ui.animelist

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.svksricharan.animeapp.domain.model.Anime
import com.svksricharan.animeapp.ui.components.ErrorScreen
import com.svksricharan.animeapp.ui.components.ImagePlaceholder
import com.svksricharan.animeapp.ui.components.LoadingScreen
import com.svksricharan.animeapp.ui.components.OfflineBanner
import com.svksricharan.animeapp.ui.components.ScoreBadge
import com.svksricharan.animeapp.ui.theme.TextGrey
import com.svksricharan.animeapp.utils.UiState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

// Start loading next page when user is within 5 items of the bottom
private const val LOAD_MORE_THRESHOLD = 5

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeListScreen(
    viewModel: AnimeListViewModel,
    showImages: Boolean,
    onToggleImages: () -> Unit,
    onAnimeClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val paginationError by viewModel.paginationError.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Top Anime",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onToggleImages) {
                        Icon(
                            imageVector = if (showImages) Icons.Default.Image else Icons.Default.ImageNotSupported,
                            contentDescription = if (showImages) "Hide images" else "Show images",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isOffline) {
                OfflineBanner()
            }

            when (val state = uiState) {
                is UiState.Loading -> LoadingScreen()
                is UiState.Error -> ErrorScreen(
                    message = state.message,
                    onRetry = { viewModel.fetchTopAnime(forceRefresh = true) }
                )
                is UiState.Success -> {
                    AnimeList(
                        animeList = state.data,
                        showImages = showImages,
                        isLoadingMore = isLoadingMore,
                        paginationError = paginationError,
                        onAnimeClick = onAnimeClick,
                        onLoadMore = { viewModel.loadNextPage() },
                        onRetryPage = { viewModel.retryNextPage() }
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimeList(
    animeList: List<Anime>,
    showImages: Boolean,
    isLoadingMore: Boolean,
    paginationError: Boolean,
    onAnimeClick: (Int) -> Unit,
    onLoadMore: () -> Unit,
    onRetryPage: () -> Unit
) {
    val listState = rememberLazyListState()

    // Using snapshotFlow instead of derivedStateOf + LaunchedEffect because
    // snapshotFlow + distinctUntilChanged guarantees exactly one emission per
    // threshold crossing, even during fast flings.
    LaunchedEffect(listState) {
        snapshotFlow {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            lastVisible >= total - LOAD_MORE_THRESHOLD
        }
            .distinctUntilChanged()
            .filter { it }
            .collect { onLoadMore() }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = animeList,
            key = { it.id }
        ) { anime ->
            AnimeCard(
                anime = anime,
                showImages = showImages,
                onClick = { onAnimeClick(anime.id) }
            )
        }

        if (isLoadingMore) {
            item(key = "loading_more") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        if (paginationError) {
            item(key = "pagination_error") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Failed to load more",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(onClick = onRetryPage) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("Retry")
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimeCard(
    anime: Anime,
    showImages: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    // Memoize the image request so we don't rebuild it on every recomposition
    val imageRequest = remember(anime.imageUrl) {
        ImageRequest.Builder(context)
            .data(anime.imageUrl)
            .crossfade(300)
            .build()
    }

    // Pre-compute this string so we don't redo the when() on every frame
    val episodeText = remember(anime.episodes, anime.airing) {
        when {
            anime.episodes != null -> "${anime.episodes} episodes"
            anime.airing == true -> "Airing"
            else -> "Unknown episodes"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // When images are toggled off (design constraint), we skip the poster
            // entirely and let the text column take full width. Rank badge moves inline.
            if (showImages) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .aspectRatio(0.7f)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    SubcomposeAsyncImage(
                        model = imageRequest,
                        contentDescription = anime.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = { ImagePlaceholder() },
                        error = { ImagePlaceholder() }
                    )

                    anime.rank?.let { rank ->
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(4.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "#$rank",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!showImages) {
                        anime.rank?.let { rank ->
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "#$rank",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = anime.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = episodeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGrey
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row {
                    anime.type?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    anime.status?.let {
                        Text(
                            text = " \u2022 $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGrey
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ScoreBadge(score = anime.score)

                Spacer(modifier = Modifier.height(6.dp))

                anime.rating?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGrey,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
