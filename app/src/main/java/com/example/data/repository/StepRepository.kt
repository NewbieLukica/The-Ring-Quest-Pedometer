package com.example.data.repository

import com.example.data.dao.StepLogDao
import com.example.data.model.StepLog
import kotlinx.coroutines.flow.Flow

class StepRepository(private val stepLogDao: StepLogDao) {
    val allStepLogs: Flow<List<StepLog>> = stepLogDao.getAllStepLogs()

    fun getStepLogByDate(date: String): Flow<StepLog?> {
        return stepLogDao.getStepLogByDate(date)
    }

    suspend fun getStepLogByDateSync(date: String): StepLog? {
        return stepLogDao.getStepLogByDateSync(date)
    }

    suspend fun insertStepLog(stepLog: StepLog) {
        stepLogDao.insertStepLog(stepLog)
    }

    suspend fun updateSteps(date: String, steps: Int) {
        stepLogDao.updateSteps(date, steps)
    }

    suspend fun deleteStepLog(date: String) {
        stepLogDao.deleteStepLog(date)
    }
}
