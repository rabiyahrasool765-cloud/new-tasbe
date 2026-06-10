package com.example.data

import kotlinx.coroutines.flow.Flow

class TasbihRepository(private val db: TasbihDatabase) {
    private val dao = db.dhikrDao()

    val allDhikrs: Flow<List<DhikrEntity>> = dao.getAllDhikrsFlow()
    val allHistory: Flow<List<DhikrHistory>> = dao.getAllHistoryFlow()

    suspend fun getDhikrById(id: Int): DhikrEntity? = dao.getDhikrById(id)

    suspend fun insertDhikr(dhikr: DhikrEntity): Long = dao.insertDhikr(dhikr)

    suspend fun updateDhikr(dhikr: DhikrEntity) = dao.updateDhikr(dhikr)

    suspend fun deleteDhikr(dhikr: DhikrEntity) = dao.deleteDhikr(dhikr)

    suspend fun deleteDhikrById(id: Int) = dao.deleteDhikrById(id)

    suspend fun insertHistory(history: DhikrHistory) = dao.insertHistory(history)

    suspend fun clearHistory() = dao.clearHistory()

    suspend fun checkAndPrepopulateDefaults() {
        val count = dao.getAllDhikrs().size
        if (count == 0) {
            val defaults = listOf(
                DhikrEntity(
                    name = "Subhan Allah",
                    arabic = "سُبْحَانَ ٱللَّٰهِ",
                    translation = "Glory be to Allah",
                    target = 33,
                    isDefault = true,
                    orderId = 1
                ),
                DhikrEntity(
                    name = "Alhamdulillah",
                    arabic = "ٱلْحَمْدُ لِلَّٰهِ",
                    translation = "Praise be to Allah",
                    target = 33,
                    isDefault = true,
                    orderId = 2
                ),
                DhikrEntity(
                    name = "Allahu Akbar",
                    arabic = "ٱللَّٰهُ أَكْبَرُ",
                    translation = "Allah is the Greatest",
                    target = 34,
                    isDefault = true,
                    orderId = 3
                ),
                DhikrEntity(
                    name = "Astaghfirullah",
                    arabic = "أَسْتَغْفِرُ ٱللَّٰهَ",
                    translation = "I seek forgiveness from Allah",
                    target = 100,
                    isDefault = true,
                    orderId = 4
                ),
                DhikrEntity(
                    name = "La ilaha illa Allah",
                    arabic = "لَا إِلَٰهَ إِلَّا ٱللَّٰهُ",
                    translation = "There is no deity but Allah",
                    target = 100,
                    isDefault = true,
                    orderId = 5
                )
            )
            dao.insertDhikrs(defaults)
        }
    }
}
