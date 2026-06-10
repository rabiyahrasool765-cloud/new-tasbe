package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.DhikrEntity
import com.example.data.DhikrHistory
import com.example.data.TasbihDatabase
import com.example.data.TasbihRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TasbihViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPrefs = application.getSharedPreferences("tasbih_preferences", Context.MODE_PRIVATE)

    private val database = Room.databaseBuilder(
        application,
        TasbihDatabase::class.java,
        "tasbih_database"
    ).build()

    private val repository = TasbihRepository(database)

    val allDhikrs: StateFlow<List<DhikrEntity>> = repository.allDhikrs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHistory: StateFlow<List<DhikrHistory>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeDhikrId = MutableStateFlow<Int>(-1)
    val activeDhikrId = _activeDhikrId.asStateFlow()

    val activeDhikr: StateFlow<DhikrEntity?> = combine(allDhikrs, activeDhikrId) { list, id ->
        if (id == -1) {
            list.firstOrNull()
        } else {
            list.find { it.id == id } ?: list.firstOrNull()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Settings
    private val _isVibrationEnabled = MutableStateFlow(sharedPrefs.getBoolean("vibration_enabled", true))
    val isVibrationEnabled = _isVibrationEnabled.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(sharedPrefs.getBoolean("sound_enabled", true))
    val isSoundEnabled = _isSoundEnabled.asStateFlow()

    private val _isAutoNextEnabled = MutableStateFlow(sharedPrefs.getBoolean("auto_next_enabled", true))
    val isAutoNextEnabled = _isAutoNextEnabled.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkAndPrepopulateDefaults()
            // Set initial active dhikr
            allDhikrs.collectFirstNonEmpty()?.let {
                _activeDhikrId.value = it.first().id
            }
        }
    }

    private suspend fun Flow<List<DhikrEntity>>.collectFirstNonEmpty(): List<DhikrEntity>? {
        return this.filter { it.isNotEmpty() }.firstOrNull()
    }

    fun setActiveDhikr(id: Int) {
        _activeDhikrId.value = id
    }

    fun toggleVibration() {
        val newVal = !_isVibrationEnabled.value
        _isVibrationEnabled.value = newVal
        sharedPrefs.edit().putBoolean("vibration_enabled", newVal).apply()
    }

    fun toggleSound() {
        val newVal = !_isSoundEnabled.value
        _isSoundEnabled.value = newVal
        sharedPrefs.edit().putBoolean("sound_enabled", newVal).apply()
    }

    fun toggleAutoNext() {
        val newVal = !_isAutoNextEnabled.value
        _isAutoNextEnabled.value = newVal
        sharedPrefs.edit().putBoolean("auto_next_enabled", newVal).apply()
    }

    // Main Counter Functions
    fun incrementCount() {
        val currentDhikr = activeDhikr.value ?: return
        viewModelScope.launch {
            val newCount = currentDhikr.count + 1
            val isTargetReached = currentDhikr.target > 0 && newCount >= currentDhikr.target

            var completedCycles = currentDhikr.totalCompletedCycles
            var finalCount = newCount

            if (isTargetReached) {
                completedCycles += 1
                finalCount = 0 // Reset current count to loop
                
                // Add completion session to history
                repository.insertHistory(
                    DhikrHistory(
                        dhikrId = currentDhikr.id,
                        dhikrName = currentDhikr.name,
                        count = currentDhikr.target
                    )
                )

                // Trigger target completion haptics
                triggerCompletionVibration()

                // Auto-transition to next preset if active
                if (_isAutoNextEnabled.value) {
                    val list = allDhikrs.value
                    val currentIndex = list.indexOfFirst { it.id == currentDhikr.id }
                    if (currentIndex != -1 && list.isNotEmpty()) {
                        val nextIndex = (currentIndex + 1) % list.size
                        _activeDhikrId.value = list[nextIndex].id
                    }
                }
            } else {
                triggerTapVibration()
            }

            val updatedDhikr = currentDhikr.copy(
                count = finalCount,
                totalPressed = currentDhikr.totalPressed + 1,
                totalCompletedCycles = completedCycles
            )
            repository.updateDhikr(updatedDhikr)
        }
    }

    fun resetCount() {
        val currentDhikr = activeDhikr.value ?: return
        if (currentDhikr.count == 0) return
        
        viewModelScope.launch {
            // Logs history on manual reset to keep trace of partially completed count
            repository.insertHistory(
                DhikrHistory(
                    dhikrId = currentDhikr.id,
                    dhikrName = currentDhikr.name,
                    count = currentDhikr.count
                )
            )

            val updatedDhikr = currentDhikr.copy(count = 0)
            repository.updateDhikr(updatedDhikr)
        }
    }

    fun decrementCount() {
        val currentDhikr = activeDhikr.value ?: return
        if (currentDhikr.count <= 0) return

        viewModelScope.launch {
            val updatedDhikr = currentDhikr.copy(
                count = currentDhikr.count - 1,
                totalPressed = (currentDhikr.totalPressed - 1).coerceAtLeast(0)
            )
            repository.updateDhikr(updatedDhikr)
        }
    }

    fun addCustomDhikr(name: String, arabic: String, translation: String, target: Int) {
        viewModelScope.launch {
            val order = (allDhikrs.value.maxOfOrNull { it.orderId } ?: 0) + 1
            val newDhikr = DhikrEntity(
                name = name,
                arabic = arabic,
                translation = translation,
                target = target,
                orderId = order
            )
            val newId = repository.insertDhikr(newDhikr)
            _activeDhikrId.value = newId.toInt()
        }
    }

    fun deleteDhikr(id: Int) {
        viewModelScope.launch {
            repository.deleteDhikrById(id)
            if (_activeDhikrId.value == id) {
                val list = allDhikrs.value.filter { it.id != id }
                if (list.isNotEmpty()) {
                    _activeDhikrId.value = list.first().id
                } else {
                    _activeDhikrId.value = -1
                }
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // Vibration Helpers
    private fun getVibrator(): Vibrator? {
        val context = getApplication<Application>().applicationContext
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun triggerTapVibration() {
        if (!_isVibrationEnabled.value) return
        val vibrator = getVibrator() ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(30)
        }
    }

    private fun triggerCompletionVibration() {
        if (!_isVibrationEnabled.value) return
        val vibrator = getVibrator() ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 150), -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 150, 100, 150), -1)
        }
    }
}
