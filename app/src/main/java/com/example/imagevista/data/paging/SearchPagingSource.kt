package com.example.imagevista.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.imagevista.data.mapper.toDomainModelList
import com.example.imagevista.data.remote.UnsplashApiService
import com.example.imagevista.domain.model.UnsplashImage

class SearchPagingSource(
    private val query: String,                      // Search Term
    private val unsplashApi: UnsplashApiService     // API Service being used
): PagingSource<Int, UnsplashImage>() {             // Int -> Page Numbers; UnsplashImage -> type of data (images from Unsplash)

    companion object {                                  // Companion Object: Only 1 instance is created for all instances of class
        private const val STARTING_PAGE_INDEX = 1
    }

    override fun getRefreshKey(state: PagingState<Int, UnsplashImage>): Int? {
        return state.anchorPosition                     // Most recent position is accessed by user, helps decide which page to refresh
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UnsplashImage> {
        val currentPage = params.key ?: STARTING_PAGE_INDEX             // params.key: page no. provided by the PagingSource
        return try{
            val response = unsplashApi.searchImages(
                query = query,
                page = currentPage,
                perPage = params.loadSize           // No. of items to load
            )
            val endOfPaginationReached = response.images.isEmpty()      // Boolean, if empty list, marks end of available page
            LoadResult.Page(
                data = response.images.toDomainModelList(),             // List of images
                prevKey = if (currentPage == STARTING_PAGE_INDEX) null else currentPage - 1,
                nextKey = if (endOfPaginationReached) null else currentPage + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }

    }
}