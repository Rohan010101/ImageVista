package com.example.imagevista.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.imagevista.data.local.db.ImageVistaDatabase
import com.example.imagevista.data.mapper.toDomainModel
import com.example.imagevista.data.mapper.toDomainModelList
import com.example.imagevista.data.mapper.toFavoriteImageEntity
import com.example.imagevista.data.paging.EditorialFeedRemoteMediator
import com.example.imagevista.data.paging.SearchPagingSource
import com.example.imagevista.data.remote.UnsplashApiService
import com.example.imagevista.data.util.Constants.ITEMS_PER_PAGE
import com.example.imagevista.domain.model.UnsplashImage
import com.example.imagevista.domain.repository.ImageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalPagingApi::class)
class ImageRepositoryImpl (
    private val unsplashApi: UnsplashApiService,
    private val database: ImageVistaDatabase
): ImageRepository {

    private val favoriteImagesDao = database.favoriteImagesDao()
    private val editorialImagesDao = database.editorialFeedDao()


    override fun getEditorialFeedImages(): Flow<PagingData<UnsplashImage>> {
        return Pager(               // helps loading paginated data, combines data from both local & remote sources
            config = PagingConfig(pageSize = ITEMS_PER_PAGE),
            remoteMediator = EditorialFeedRemoteMediator(unsplashApi, database),        // fetches data from a remote source & saves it into a local database
            pagingSourceFactory = { editorialImagesDao.getAllEditorialFeedImages()}     // tells where to get the paginated data from locally
        )
            .flow
            .map { pagingData ->
                pagingData.map { it.toDomainModel()}
            }
    }

    override suspend fun getImage(imageId: String): UnsplashImage {
        return unsplashApi.getImage(imageId).toDomainModel()
    }

    // PagingData: holds the paginated list of UnsplashImage
    // Returns a flow of PagingData<UnsplashImage>
    override fun searchImages(query: String): Flow<PagingData<UnsplashImage>> {
        return Pager(               // Executes Paging Logic. Manages the data source & controls how pages are loaded (no. of items to load per page)
            config = PagingConfig(pageSize = ITEMS_PER_PAGE),                       // Defines Paging Configuration
            pagingSourceFactory = { SearchPagingSource(query, unsplashApi)}         // creates new instance of SearchPagingSource, with each paging request
        ).flow              // Converts the Pager into a Flow of PagingData, allows asynchronous data collection
    }


    override suspend fun toggleFavoriteStatus(image: UnsplashImage) {
        val isFavorite = favoriteImagesDao.isImageFavorite(image.id)
        val favoriteImage = image.toFavoriteImageEntity()
        if (isFavorite) {
            favoriteImagesDao.deleteFavoriteImage(favoriteImage)
        } else {
            favoriteImagesDao.insertFavoriteImage(favoriteImage)
        }
    }

    override fun getFavoriteImageIds(): Flow<List<String>> {
        return favoriteImagesDao.getFavoriteImageIds()
    }

    override fun getAllFavoriteImages(): Flow<PagingData<UnsplashImage>> {
        return Pager(
            config = PagingConfig(pageSize = ITEMS_PER_PAGE),
            pagingSourceFactory = { favoriteImagesDao.getAllFavoriteImages()}
        )
            .flow
            .map { pagingData ->
                pagingData.map { it.toDomainModel()}
            }
    }
}