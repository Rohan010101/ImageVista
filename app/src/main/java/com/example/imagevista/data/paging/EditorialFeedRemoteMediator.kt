package com.example.imagevista.data.paging

import android.util.Log
import androidx.compose.ui.unit.Constraints
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.LoadType.*
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.imagevista.data.local.db.ImageVistaDatabase
import com.example.imagevista.data.local.entity.UnsplashImageEntity
import com.example.imagevista.data.local.entity.UnsplashRemoteKeys
import com.example.imagevista.data.mapper.toEntityList
import com.example.imagevista.data.remote.UnsplashApiService
import com.example.imagevista.data.util.Constants
import com.example.imagevista.data.util.Constants.ITEMS_PER_PAGE

@OptIn(ExperimentalPagingApi::class)
class EditorialFeedRemoteMediator(
    private val apiService: UnsplashApiService,
    private val database: ImageVistaDatabase
): RemoteMediator<Int, UnsplashImageEntity>() {

    companion object {
        private const val STARTING_PAGE_INDEX = 1
    }


    private val editorialFeedDao = database.editorialFeedDao()


    // MAIN PAGE LOGIC

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, UnsplashImageEntity>
    ): MediatorResult {
        try {
            val currentPage = when(loadType) {
                // Refreshes the whole feed
                REFRESH -> {
                    val remoteKeys = getRemoteKeysClosestToCurrentPosition(state)
                    remoteKeys?.nextPage?.minus(1)?: STARTING_PAGE_INDEX
                }

                // Loading previous page
                PREPEND -> {
                    val remoteKeys = getRemoteKeysForFirstItem(state)
                    Log.d(Constants.IV_LOG_TAG, "remoteKeysPrev: ${remoteKeys?.prevPage}")
                    val prevPage = remoteKeys?.prevPage
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                    prevPage
                }

                // Loading next page
                APPEND -> {
                    val remoteKeys = getRemoteKeysForLastItem(state)
                    Log.d(Constants.IV_LOG_TAG, "remoteKeysNext: ${remoteKeys?.nextPage}")
                    val nextPage = remoteKeys?.nextPage
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                    nextPage
                }
            }

            // API call to get next set of images from the server
            val response =
                apiService.getEditorialFeedImages(page = currentPage, perPage = ITEMS_PER_PAGE)

            // Checks if there is no more pages to load
            val endOfPaginationReached = response.isEmpty()
            Log.d(Constants.IV_LOG_TAG, "endOfPaginationReached: $endOfPaginationReached")


            // calculates previous & next page numbers
            val prevPage = if (currentPage == 1) null else currentPage - 1
            val nextPage = if (endOfPaginationReached) null else currentPage + 1

            // the images fetched from the API is now stored locally into Local Database
            database.withTransaction {

                // deletes the old images from local DB when refreshed
                // save newly fetched image from the local DB
                if (loadType == REFRESH) {
                    editorialFeedDao.deleteAllEditorialFeedImages()
                    editorialFeedDao.deleteAllRemoteKeys()
                }

                // Keeps a track of next page & previous page
                val remoteKeys = response.map { unsplashImageDto ->
                    UnsplashRemoteKeys(
                        id = unsplashImageDto.id,
                        prevPage = prevPage,
                        nextPage = nextPage
                    )
                }
                editorialFeedDao.insertAllRemoteKeys(remoteKeys)
                editorialFeedDao.insertEditorialFeedImages(response.toEntityList())
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)

        } catch (e: Exception) {
            Log.d(Constants.IV_LOG_TAG, "LoadResultError: ${e.message}")
            return MediatorResult.Error(e)
        }
    }


    // for Prepend Operation, looks at the first image currently loaded & its pagination
    suspend private fun getRemoteKeysClosestToCurrentPosition(
        state: PagingState<Int, UnsplashImageEntity>
    ): UnsplashRemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { unsplashImage ->
                editorialFeedDao.getRemoteKeys(id = unsplashImage.id)
            }
    }

    // for Prepend Operation, looks at the first image currently loaded & its pagination
    suspend private fun getRemoteKeysForFirstItem(
        state: PagingState<Int, UnsplashImageEntity>
    ): UnsplashRemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { unsplashImage ->
                editorialFeedDao.getRemoteKeys(id = unsplashImage.id)
            }
    }

    // for Append Operation, looks at the last image currently loaded & its pagination
    suspend private fun getRemoteKeysForLastItem(
        state: PagingState<Int, UnsplashImageEntity>
    ): UnsplashRemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { unsplashImage ->
                editorialFeedDao.getRemoteKeys(id = unsplashImage.id)
            }
    }
}