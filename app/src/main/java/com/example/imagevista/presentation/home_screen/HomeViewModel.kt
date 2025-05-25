package com.example.imagevista.presentation.home_screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.imagevista.data.mapper.toDomainModelList
import com.example.imagevista.di.AppModule
import com.example.imagevista.domain.model.UnsplashImage
import com.example.imagevista.domain.repository.ImageRepository
import com.example.imagevista.presentation.util.SnackbarEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ImageRepository
): ViewModel() {

    private val _snackbarEvent = Channel<SnackbarEvent>()
    val snackbarEvent = _snackbarEvent.receiveAsFlow()


    //
    val images: StateFlow<PagingData<UnsplashImage>> = repository.getEditorialFeedImages()
        .catch { exception ->
            _snackbarEvent.send(
                SnackbarEvent(message = "Something went wrong. ${exception.message}")
            )
        }
        .cachedIn(viewModelScope)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = PagingData.empty()
        )


    // liked images collection
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


    // like / dislike
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






//    var images: List<UnsplashImage> by mutableStateOf(emptyList())
//        private set
//
//    init {
//        getImages()
//    }
//
//    private fun getImages() {
//        viewModelScope.launch {
//            try {
//                val result = repository.getEditorialFeedImages()
//                images = result
//            } catch (e: UnknownHostException) {
//                _snackbarEvent.send(
//                    SnackbarEvent(message = "No Internet Connection. Please check your network.")
//                )
//            } catch (e: Exception) {
//                _snackbarEvent.send(
//                    SnackbarEvent(message = "Something went wrong: ${e.message}")
//                )
//            }
//        }
//    }
}