package com.example.assignment3_part1

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SensorViewModel(application: Application) : AndroidViewModel(application) {
    private val db = SensorDatabase.getDatabase(application)
    private val sensorDao = db.sensorDataDao()
    fun insert(sensorData: SensorData) {
        CoroutineScope(Dispatchers.IO).launch {
            sensorDao.insert(sensorData)
        }
    }

    suspend fun getAll(): List<SensorData> {
        return sensorDao.getAll()
    }

    suspend fun getSensorData(): SensorData {
//        return the latest sensor data
        return sensorDao.getAll().first()
    }

    suspend fun deleteAll() {
        sensorDao.deleteAll()
    }
}