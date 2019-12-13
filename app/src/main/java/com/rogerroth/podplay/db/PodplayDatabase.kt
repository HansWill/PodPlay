package com.rogerroth.podplay.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.rogerroth.podplay.model.Episode
import com.rogerroth.podplay.model.Podcast
import java.util.*

class Converters {
	@TypeConverter
	fun fromTimeStamp(value: Long?): Date? {
		return if (value == null) null else Date(value)
	}
	@TypeConverter
	fun toTimeStamp(date: Date?): Long? {
		return (date?.time)
	}
}

@Database(entities = arrayOf(Podcast::class, Episode::class), version = 1)
@TypeConverters(Converters::class)
abstract class PodplayDatabase : RoomDatabase() {
	abstract fun podcastDao(): PodcastDao

	companion object {
		private var instance: PodplayDatabase? = null

		fun getInstance(context: Context): PodplayDatabase {
			if (instance == null) {
				instance = Room.databaseBuilder(context.applicationContext, PodplayDatabase::class.java, "PodPlayer").build()
			}
			return instance as PodplayDatabase
		}
	}
}