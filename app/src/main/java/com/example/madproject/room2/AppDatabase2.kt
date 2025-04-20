package com.example.madproject.room2



import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
@Database(entities = [CoordinatesEntity2::class], version = 1, exportSchema = false)
abstract class AppDatabase2 : RoomDatabase() {
    abstract fun coordinatesDao(): ICoordinatesDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase2? = null
        fun getDatabase(context: Context): AppDatabase2 {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase2::class.java,
                    "coordinates_database_list"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
