package com.gainus.gaiapp.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual fun getDatabaseBuilder(context: Any?): RoomDatabase.Builder<AppDatabase> {
    val appContext =
        (context as? Context)
            ?: throw IllegalArgumentException("Context required for Android database")
    val dbFile = appContext.getDatabasePath("app_database.db")
    return Room.databaseBuilder<AppDatabase>(context = appContext, name = dbFile.absolutePath)
}
