package com.mypalli.pallismsparser.dataclasses
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//class DatabaseClass {
//}

//@Database(entities = [SMSDataEntity::class], version = 1, exportSchema = false)
//abstract class AppDatabase : RoomDatabase() {
//    abstract fun smsDao(): SMSDao
//}

@Database(entities = [SMSDataEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun smsDao(): SMSDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sms_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

