package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SaveStateDao {
    @Query("SELECT * FROM save_state WHERE id = 1 LIMIT 1")
    fun getSaveStateFlow(): Flow<SaveState?>

    @Query("SELECT * FROM save_state WHERE id = 1 LIMIT 1")
    suspend fun getSaveStateDirect(): SaveState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(saveState: SaveState)

    @Query("DELETE FROM save_state")
    suspend fun clearAll()
}
