package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.StepLog
import kotlinx.coroutines.flow.Flow

@Dao
interface StepLogDao {
    @Query("SELECT * FROM step_logs ORDER BY date DESC")
    fun getAllStepLogs(): Flow<List<StepLog>>

    @Query("SELECT * FROM step_logs WHERE date = :date LIMIT 1")
    fun getStepLogByDate(date: String): Flow<StepLog?>

    @Query("SELECT * FROM step_logs WHERE date = :date LIMIT 1")
    suspend fun getStepLogByDateSync(date: String): StepLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStepLog(stepLog: StepLog)

    @Query("UPDATE step_logs SET steps = :steps WHERE date = :date")
    suspend fun updateSteps(date: String, steps: Int)

    @Query("DELETE FROM step_logs WHERE date = :date")
    suspend fun deleteStepLog(date: String)
}
