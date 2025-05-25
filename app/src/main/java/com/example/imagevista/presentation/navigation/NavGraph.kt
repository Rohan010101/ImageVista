package com.example.imagevista.presentation.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.imagevista.presentation.favorites_screen.FavoritesScreen
import com.example.imagevista.presentation.favorites_screen.FavoritesViewModel
import com.example.imagevista.presentation.full_image_screen.FullImageScreen
import com.example.imagevista.presentation.full_image_screen.FullImageViewModel
import com.example.imagevista.presentation.home_screen.HomeScreen
import com.example.imagevista.presentation.home_screen.HomeViewModel
import com.example.imagevista.presentation.profile_screen.ProfileScreen
import com.example.imagevista.presentation.search_screen.SearchScreen
import com.example.imagevista.presentation.search_screen.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraphSetup(
    navController: NavHostController,
    scrollBehavior: TopAppBarScrollBehavior,
    snackbarHostState: SnackbarHostState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    ) {
    NavHost(
        navController = navController,
        startDestination = Routes.HomeScreen
    ) {
        composable<Routes.HomeScreen> {
            val homeViewModel : HomeViewModel = hiltViewModel()
            val images = homeViewModel.images.collectAsLazyPagingItems()
            val favoriteImageIds by homeViewModel.favoriteImageIds.collectAsState()
            HomeScreen(
                snackbarEvent = homeViewModel.snackbarEvent,
                snackbarHostState = snackbarHostState,
                scrollBehavior = scrollBehavior,
                images = images,
                favoriteImageIds = favoriteImageIds,
                onImageClick = { imageId ->
                    navController.navigate(Routes.FullImageScreen(imageId))
                },
                onSearchClick = { navController.navigate(Routes.SearchScreen) },
                onFABClick = {navController.navigate(Routes.FavoritesScreen)},
                onToggleFavoriteStatus = {homeViewModel.toggleFavoriteStatus(it)}
            )
        }
        composable<Routes.SearchScreen> {
            val searchViewModel: SearchViewModel = hiltViewModel()
            val searchedImages = searchViewModel.searchImages.collectAsLazyPagingItems()
            val favoriteImageIds by searchViewModel.favoriteImageIds.collectAsState()
            SearchScreen(
                snackbarHostState = snackbarHostState,
                snackbarEvent = searchViewModel.snackbarEvent,
                favoriteImageIds = favoriteImageIds,
                onBackClick = { navController.navigateUp() },
                searchedImages = searchedImages,
                searchQuery = searchQuery,
                OnSearchQueryChange = onSearchQueryChange,
                onImageClick = {imageId ->
                    navController.navigate(Routes.FullImageScreen(imageId))
                },
                onSearch = {searchViewModel.searchImages(it)},
                onToggleFavoriteStatus = {searchViewModel.toggleFavoriteStatus(it)}
            )
        }
        composable<Routes.FavoritesScreen> {
            val favoritesViewModel: FavoritesViewModel = hiltViewModel()
            val favoriteImages = favoritesViewModel.favoriteImages.collectAsLazyPagingItems()
            val favoriteImageIds by favoritesViewModel.favoriteImageIds.collectAsStateWithLifecycle()

            FavoritesScreen(
                snackbarHostState = snackbarHostState,
                favoriteImages = favoriteImages,
                snackbarEvent = favoritesViewModel.snackbarEvent,
                favoriteImageIds = favoriteImageIds,
                scrollBehavior = scrollBehavior,
                onSearchClick = { navController.navigate(Routes.SearchScreen)},
                onImageClick = {imageId ->
                    navController.navigate(Routes.FullImageScreen(imageId))
                },
                onBackClick = { navController.navigateUp() },
                onToggleFavoriteStatus = { favoritesViewModel.toggleFavoriteStatus(it)}
            )
        }
        composable<Routes.FullImageScreen> {
            val fullImageViewModel: FullImageViewModel = hiltViewModel()
            FullImageScreen(
                snackbarEvent = fullImageViewModel.snackbarEvent,
                snackbarHostState = snackbarHostState,
                image = fullImageViewModel.image,
                onBackClick = {navController.navigateUp() },
                onPhotographerImgClick = { profileLink ->
                    navController.navigate(Routes.ProfileScreen(profileLink))
                },
                onImageDownloadClick = { url, title ->
                    fullImageViewModel.downloadImage(url, title)
                }
            )
        }
        composable<Routes.ProfileScreen> { backStackEntry ->
            val profileLink = backStackEntry.toRoute<Routes.ProfileScreen>().profileLink
            ProfileScreen(
                profileLink =profileLink ,
                onBackClick = {navController.navigateUp()}
            )
        }
    }
}