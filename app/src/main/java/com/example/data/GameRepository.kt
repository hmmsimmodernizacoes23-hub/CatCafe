package com.example.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val dao: SaveStateDao) {
    val saveStateFlow: Flow<SaveState?> = dao.getSaveStateFlow()

    suspend fun getSaveStateDirect(): SaveState? {
        return dao.getSaveStateDirect()
    }

    suspend fun saveProgress(saveState: SaveState) {
        dao.insertOrUpdate(saveState)
    }

    suspend fun resetProgress() {
        dao.clearAll()
        // Save standard starting state
        dao.insertOrUpdate(SaveState())
    }
}
