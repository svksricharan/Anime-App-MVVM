package com.svksricharan.animeapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.svksricharan.animeapp.data.local.dao.AnimeDao
import com.svksricharan.animeapp.data.local.entity.AnimeEntity

// Using fallbackToDestructiveMigration because the schema changed a few times
// during development (added page column, trailer fields, etc). For production
// I'd write proper migrations, but for an assignment this keeps things clean.
@Database(
    entities = [AnimeEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AnimeDatabase : RoomDatabase() {

    abstract fun animeDao(): AnimeDao

    // Thread-safe singleton — double-checked locking pattern
    companion object {
        @Volatile
        private var INSTANCE: AnimeDatabase? = null

        fun getInstance(context: Context): AnimeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AnimeDatabase::class.java,
                    "anime_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
