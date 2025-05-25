package com.example.imagevista.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.imagevista.data.local.dao.EditorialFeedDao
import com.example.imagevista.data.local.dao.FavoriteImagesDao
import com.example.imagevista.data.local.entity.FavoriteImageEntity
import com.example.imagevista.data.local.entity.UnsplashImageEntity
import com.example.imagevista.data.local.entity.UnsplashRemoteKeys


@Database(
    entities = [FavoriteImageEntity::class, UnsplashImageEntity::class, UnsplashRemoteKeys::class],
    version = 1,
    exportSchema = false
)
abstract class ImageVistaDatabase: RoomDatabase() {

    abstract fun favoriteImagesDao(): FavoriteImagesDao

    abstract fun editorialFeedDao(): EditorialFeedDao
}