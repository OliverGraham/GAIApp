package com.gai.gaiapp.data.local.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// This TaskDao fulfills part of TODO-014: Add task database schema

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks") fun observeAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertTask(task: TaskEntity)

    @Update suspend fun updateTask(task: TaskEntity)

    @Delete suspend fun deleteTask(task: TaskEntity)
}
