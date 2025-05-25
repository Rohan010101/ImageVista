package com.example.imagevista.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.imagevista.data.local.entity.FavoriteImageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteImagesDao {

    @Query("SELECT * FROM favorite_images_table")
    fun getAllFavoriteImages(): PagingSource<Int, FavoriteImageEntity>

    @Upsert
    suspend fun insertFavoriteImage(image: FavoriteImageEntity)

    // JUST THE DATA IS BEING DELETED FROM DB
    // But the image card still exists over there, hence changing aspect ratio of other images
    // Need to destroy the Image Card itself!!!!!!!!!!
    @Delete
    suspend fun deleteFavoriteImage(image: FavoriteImageEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_images_table WHERE id = :id)")
    suspend fun isImageFavorite(id: String): Boolean

    @Query("SELECT id FROM favorite_images_table")
    fun getFavoriteImageIds(): Flow<List<String>>
}