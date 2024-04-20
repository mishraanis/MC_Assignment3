package com.example.assignment3_part1

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction

@Entity
data class SensorData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val azimuth: Float,
    val pitch: Float,
    val roll: Float,
    val timestamp: Long
)

@Dao
interface SensorDataDao {
    @Insert
    suspend fun insert(sensorData: SensorData)

    @Query("SELECT * FROM SensorData ORDER BY timestamp DESC")
    suspend fun getAll(): List<SensorData>

    @Query("DELETE FROM SensorData")
    suspend fun deleteAll()
}

@Database(entities = [SensorData::class], version = 1)
abstract class SensorDatabase : RoomDatabase() {
    abstract fun sensorDataDao(): SensorDataDao
    companion object {
        @Volatile
        private var INSTANCE: SensorDatabase? = null

        fun getDatabase(context: Context): SensorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SensorDatabase::class.java,
                    "sensor-database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}


