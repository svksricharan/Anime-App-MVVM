# AnimeApp — Seekho Android Developer Assignment

An Android app that fetches and displays top-rated anime from the Jikan API, with detail pages, trailers, offline support, 
and paginated browsing. Built with Jetpack Compose and MVVM.


## Architecture Overview

The app follows MVVM with a clear separation between UI, domain, and data layers.

### Data Flow

Repository fetches from the Jikan API via Retrofit. The response DTOs get mapped to AnimeEntity and cached in Room. 
Then those DTOs or entities are mapped to a clean Anime domain model before reaching the UI. 
The ViewModel exposes StateFlow of UiState, and Compose screens collect it in a lifecycle-aware way. 
If the network call fails, the Repository falls back to whatever is cached in Room.

### Layer Responsibilities

- UI layer (Screen.kt files and ViewModels) just renders state and handles user events. No business logic here.
- Domain layer (Anime.kt, TrailerAction sealed class) is pure Kotlin with no Android imports. This is what the UI actually works with.
- Data layer (AnimeRepository, Mappers) handles network-first fetching, cache management, and all the DTO to Entity to Domain mapping.
- Local layer (AnimeDao, AnimeDatabase) handles Room persistence with paginated queries.
- Remote layer (JikanApiService) is just the Retrofit interface.
- DI layer (AppContainer) does manual singleton wiring. No Dagger or Hilt.

### Design Decisions

- Used manual DI instead of Dagger/Hilt because the dependency graph is small and it keeps things simple while still showing DI understanding.
- Kept the domain model (Anime) separate from DTOs and Room entities so the UI never depends on the API shape. If the API response changes, the UI layer doesn't need to be touched.
- Created a TrailerAction sealed class to encapsulate the 4-tier trailer resolution logic in the data layer. The UI just pattern-matches on it.
- Used BuildConfig.SHOW_IMAGES plus a runtime toggle for the design constraint requirement. Compile-time flag for legal compliance, runtime toggle for demo.
- Used snapshotFlow for the pagination scroll trigger instead of derivedStateOf because it emits exactly once per threshold crossing and avoids duplicate load calls during fast scrolling.
- Added a Mutex in the Repository to prevent concurrent cache writes from racing during rapid page loads.
- Used collectAsStateWithLifecycle everywhere so collection stops when the UI is backgrounded. No background work, no leaks.
- Added a 400ms navigation debounce to prevent duplicate screen pushes on double-tap.
- Picked Coil over Glide/Picasso because it has first-class Compose support with SubcomposeAsyncImage and is coroutine-native.


## Features — Mapped to Assignment

### 1. Anime List Page
Fetches top-rated anime from GET /v4/top/anime. Each card shows the title, episode count, MAL score, poster image, rank badge, type, and status. 
Supports infinite scroll pagination —> loads the next page automatically as the user scrolls near the bottom.

### 2. Anime Detail Page
Opens on click via GET /v4/anime/{anime_id}. Shows the trailer (play button opens YouTube app or browser), title in English and Japanese, expandable synopsis, genre chips, episode count, rating, and status. 
Trailer resolution follows a priority: youtube_id first, then url, then embed_url (shown as unavailable since YouTube blocks it in WebViews), and finally poster only if nothing is available.

### 3. Local Database (Room)
All fetched anime are cached in Room with their page numbers for paginated retrieval. 
Data syncs automatically when the app goes online through a network observer that triggers a refresh.

### 4. Offline Mode and Syncing
The app loads cached data from Room when there is no internet. An offline banner shows at the top of the list screen. 
NetworkHelper monitors connectivity using ConnectivityManager callbacks exposed as a Flow.
When connectivity comes back, the app auto-retries if the screen was stuck on an error state.

### 5. Error Handling
API errors are caught in the Repository and surfaced as UiState.Error with a retry button. 
Pagination errors show a "Failed to load more" message with a Retry button at the bottom of the list without breaking the scroll. 
Network changes are monitored in real time with auto-recovery. Database errors are wrapped in Result and propagated to the UI gracefully.

### 6. Design Constraint Handling
The assignment says: "Suppose you cannot show profile images due to a legal change." This is handled with a hybrid approach. 
There is a BuildConfig.SHOW_IMAGES flag in build.gradle.kts that sets the compile-time default — set it to false to ship without images. 
There is also a runtime toggle icon in the TopAppBar on the list screen that controls image visibility globally across both screens. 
When images are off, list cards drop the poster and the text takes full width with the rank badge moving inline. 
On the detail page, the poster area shows the anime title on a dark background instead. The layout never breaks and all content stays readable.

### 7. Edge Cases and Robustness
Duplicate anime IDs are filtered before appending during pagination. snapshotFlow with distinctUntilChanged ensures only one load trigger per scroll threshold crossing. 
Mutex is used for thread-safe cache writes. Separate fetchJob and paginationJob are properly cancelled on refresh. 
Image requests and computed text are memoized with remember to avoid unnecessary recomposition.


## Tech Stack

- Jetpack Compose with Material 3 for the UI
- Retrofit 2 with OkHttp for networking
- Gson for JSON parsing
- Room for local database
- Coil for image loading and caching
- Navigation Compose for screen routing
- Kotlin Coroutines and StateFlow for async and reactive state
- Material Icons Extended for UI icons


## API Endpoints

- GET /v4/top/anime?page={n} — paginated list of top anime
- GET /v4/anime/{id} — anime detail including synopsis, genres, and trailer info

Base URL is https://api.jikan.moe/v4/ and no API key is required.


## Project Structure

- AnimeApplication.kt — app entry point, initializes the DI container
- data/api/JikanApiService.kt — Retrofit interface
- data/local/AnimeDatabase.kt — Room database
- data/local/dao/AnimeDao.kt — DAO with paginated queries
- data/local/entity/AnimeEntity.kt — Room entity
- data/model/AnimeResponse.kt — API DTOs
- data/model/Mappers.kt — DTO to Entity to Domain mappers
- data/repository/AnimeRepository.kt — network-first with cache fallback
- di/AppContainer.kt — manual DI container
- domain/model/Anime.kt — domain model and TrailerAction sealed class
- ui/animelist/ — list screen and ViewModel
- ui/animedetail/ — detail screen and ViewModel
- ui/components/CommonUI.kt — shared composables like loading, error, shimmer
- ui/main/MainActivity.kt — single Activity entry point
- ui/navigation/AppNavigation.kt — nav graph and image toggle state
- ui/theme/ — Color, Theme, Typography
- utils/NetworkHelper.kt — connectivity observer using Flow
- utils/UiState.kt — sealed interface for Loading, Success, Error


## Assumptions

1. YouTube blocks embedded WebView playback with Error 153, so trailers open externally via ACTION_VIEW intent which is the standard Android approach.
2. The /v4/anime/{id} detail endpoint does not include full cast data. A separate /characters call was intentionally avoided to keep the detail page to a single API call.
3. The design constraint requirement was interpreted as a global image toggle. BuildConfig.SHOW_IMAGES sets the compile-time default and a runtime toggle is available in the app bar.
4. Each page of results is cached in Room with its page number. Offline mode serves all previously fetched pages.
5. Jikan API allows roughly 3 requests per second. Caching minimizes API calls but no client-side throttling is implemented.


## Known Limitations

1. Search is not implemented. Could be added using /v4/anime?q= endpoint.
2. Coil handles memory and disk caching for images but they are not stored in Room for true offline image support.
3. Rapid pagination may occasionally hit Jikan's rate limit. This is handled gracefully with a retry UI.
4. Full voice actor and character list requires an extra API call which is not made.


## How to Build

1. Clone the repository
2. Open in Android Studio (Hedgehog or later)
3. Sync Gradle and build
4. Run on emulator or device (min SDK 24)

No API key setup is required.
