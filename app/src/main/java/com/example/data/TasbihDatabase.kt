package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "dhikrs")
data class DhikrEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val arabic: String = "",
    val translation: String = "",
    val count: Int = 0,
    val target: Int = 33, // 0 for unlimited
    val totalCompletedCycles: Int = 0,
    val totalPressed: Int = 0,
    val isDefault: Boolean = false,
    val orderId: Int = 0
)

@Entity(tableName = "dhikr_history")
data class DhikrHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dhikrId: Int,
    val dhikrName: String,
    val count: Int,
    val date: Long = System.currentTimeMillis()
)

@Dao
interface DhikrDao {
    @Query("SELECT * FROM dhikrs ORDER BY orderId ASC, id ASC")
    fun getAllDhikrsFlow(): Flow<List<DhikrEntity>>

    @Query("SELECT * FROM dhikrs ORDER BY orderId ASC, id ASC")
    suspend fun getAllDhikrs(): List<DhikrEntity>

    @Query("SELECT * FROM dhikrs WHERE id = :id")
    suspend fun getDhikrById(id: Int): DhikrEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDhikr(dhikr: DhikrEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDhikrs(dhikrs: List<DhikrEntity>)

    @Update
    suspend fun updateDhikr(dhikr: DhikrEntity)

    @Delete
    suspend fun deleteDhikr(dhikr: DhikrEntity)

    @Query("SELECT * FROM dhikr_history ORDER BY date DESC LIMIT 100")
    fun getAllHistoryFlow(): Flow<List<DhikrHistory>>

    @Query("DELETE FROM dhikrs WHERE id = :id")
    suspend fun deleteDhikrById(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: DhikrHistory)

    @Query("DELETE FROM dhikr_history")
    suspend fun clearHistory()
}

@Database(entities = [DhikrEntity::class, DhikrHistory::class], version = 1, exportSchema = false)
abstract class TasbihDatabase : RoomDatabase() {
    abstract fun dhikrDao(): DhikrDao
}
