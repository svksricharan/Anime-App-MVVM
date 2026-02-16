package com.svksricharan.animeapp.ui.animedetail

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.svksricharan.animeapp.domain.model.TrailerAction
import com.svksricharan.animeapp.ui.components.ErrorScreen
import com.svksricharan.animeapp.ui.components.ImagePlaceholder
import com.svksricharan.animeapp.ui.components.LoadingScreen
import com.svksricharan.animeapp.ui.components.ScoreBadge
import com.svksricharan.animeapp.ui.theme.TextGrey
import com.svksricharan.animeapp.utils.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeDetailScreen(
    viewModel: AnimeDetailViewModel,
    showImages: Boolean,
    onBackClick: () -> Unit
) {
    val animeState by viewModel.animeState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = remember(animeState) {
                        when (val state = animeState) {
                            is UiState.Success -> state.data.title
                            else -> "Anime Detail"
                        }
                    }
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
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
        when (val state = animeState) {
            is UiState.Loading -> LoadingScreen(modifier = Modifier.padding(paddingValues))
            is UiState.Error -> ErrorScreen(
                message = state.message,
                onRetry = { viewModel.fetchAnimeDetail() },
                modifier = Modifier.padding(paddingValues)
            )
            is UiState.Success -> {
                AnimeDetailContent(
                    anime = state.data,
                    showImages = showImages,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnimeDetailContent(
    anime: Anime,
    showImages: Boolean,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        TrailerOrPoster(anime = anime, showImages = showImages)

        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = anime.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            anime.titleJapanese?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGrey
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ScoreBadge(score = anime.score)

                anime.episodes?.let {
                    InfoChip(label = "$it episodes")
                }

                anime.type?.let {
                    InfoChip(label = it)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                anime.status?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall, color = TextGrey)
                }
                anime.rating?.let {
                    Text(text = "Rating: $it", style = MaterialTheme.typography.bodySmall, color = TextGrey)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (anime.genres.isNotEmpty()) {
                Text(
                    text = "Genres",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    anime.genres.forEach { genre ->
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = genre,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                labelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            anime.synopsis?.let { synopsis ->
                Text(
                    text = "Plot / Synopsis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExpandableText(text = synopsis)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Hero area at the top of the detail page.
// Shows poster when images are on, title text when off.
// Play button overlay appears regardless — tapping opens YouTube externally.
@Composable
private fun TrailerOrPoster(anime: Anime, showImages: Boolean) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (showImages) {
            val imageRequest = remember(anime.largeImageUrl, anime.imageUrl) {
                ImageRequest.Builder(context)
                    .data(anime.largeImageUrl ?: anime.imageUrl)
                    .crossfade(300)
                    .build()
            }
            SubcomposeAsyncImage(
                model = imageRequest,
                contentDescription = anime.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = { ImagePlaceholder() },
                error = { ImagePlaceholder() }
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                anime.titleJapanese?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Trailer handling — the sealed class keeps this clean.
        // InternalPlayer and ExternalLink both open YouTube externally
        // because embedding in WebView gives Error 153.
        when (val action = anime.trailerAction) {
            is TrailerAction.InternalPlayer -> {
                val watchUrl = remember(action.youtubeId) {
                    "https://www.youtube.com/watch?v=${action.youtubeId}"
                }
                PlayButtonOverlay(
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(watchUrl))
                        )
                    }
                )
            }

            is TrailerAction.ExternalLink -> {
                PlayButtonOverlay(
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(action.url))
                        )
                    }
                )
            }

            is TrailerAction.Unavailable -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Trailer unavailable",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }

            is TrailerAction.None -> {
                // No trailer data — poster only (or title if images off), no overlay
            }
        }
    }
}

@Composable
private fun PlayButtonOverlay(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play Trailer",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

// Synopsis can be long — collapse by default, show "Read more" if > 200 chars.
// animateContentSize gives a smooth expand/collapse without manual animation code.
@Composable
private fun ExpandableText(
    text: String,
    maxLines: Int = 4
) {
    var expanded by remember { mutableStateOf(false) }
    val needsExpansion = remember(text) { text.length > 200 }

    Column(modifier = Modifier.animateContentSize()) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = if (expanded) Int.MAX_VALUE else maxLines,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
        )
        if (needsExpansion) {
            Text(
                text = if (expanded) "Show less" else "Read more",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable { expanded = !expanded }
            )
        }
    }
}

@Composable
private fun InfoChip(label: String) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}
