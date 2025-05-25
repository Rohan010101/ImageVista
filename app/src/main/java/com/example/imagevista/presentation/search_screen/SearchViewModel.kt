package com.example.imagevista.presentation.search_screen

import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.imagevista.domain.model.UnsplashImage
import com.example.imagevista.domain.repository.ImageRepository
import com.example.imagevista.presentation.util.SnackbarEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SearchViewModel @Inject constructor(      // injects ImageRepository for data handling
    private val repository: ImageRepository
): ViewModel() {                // extends ViewModel => can survive configuration changes like screen rotation


    // SNACKBAR EVENT
    private val _snackbarEvent = Channel<SnackbarEvent>()       // Kotlin Coroutine to send one-time snackbar events
    val snackbarEvent = _snackbarEvent.receiveAsFlow()          // converts channel into flow,allowing UI to listen & react to events Asynchronously



    // PAGING DATA STATE MANAGEMENT
    private val _searchImages = MutableStateFlow<PagingData<UnsplashImage>>(PagingData.empty())     // Holds the current PagingData of UnsplashImage
    val searchImages = _searchImages         // exposes the private val _searchImages to UI

    fun searchImages(query: String) {           // search query
        viewModelScope.launch {                 // launches coroutine tied to ViewModel's Lifecycle
            try {
                repository
                    .searchImages(query)                    // calls the searchImages() from Repository which returns Flow<PagingData<UnsplashImage>>
                    .cachedIn(viewModelScope)               // caches the paginated data in ViewModelScope ensuring data preservation during config change
                    .collect {_searchImages.value = it}     // when a search is requested & the pageData is ready, the collect fun listens to this emission & updates the _searchImage.value with new data
            } catch (e: Exception) {
                _snackbarEvent.send(
                    SnackbarEvent(message = "Something went wrong. ${e.message}")
                )
            }
        }
    }


    val favoriteImageIds: StateFlow<List<String>> = repository.getFavoriteImageIds()
        .catch { exception ->
            _snackbarEvent.send(
                SnackbarEvent(message = "Something went wrong. ${exception.message}")
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = emptyList()
        )

    fun toggleFavoriteStatus(image: UnsplashImage) {
        viewModelScope.launch {
            try {
                repository.toggleFavoriteStatus(image)
            } catch (e: Exception) {
                _snackbarEvent.send(
                    SnackbarEvent(message = "Something went wrong. ${e.message}")
                )
            }
        }
    }

}