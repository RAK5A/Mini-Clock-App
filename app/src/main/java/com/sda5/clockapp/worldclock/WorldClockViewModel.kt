package com.sda5.clockapp.worldclock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sda5.clockapp.data.WorldClockDao
import com.sda5.clockapp.model.WorldClockCity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorldClockViewModel(private val dao: WorldClockDao) : ViewModel() {

    val cities: StateFlow<List<WorldClockCity>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCity(zoneId: String, displayName: String) = viewModelScope.launch {
        dao.upsert(
            WorldClockCity(
                id = System.currentTimeMillis(),
                zoneId = zoneId,
                displayName = displayName,
                sortOrder = cities.value.size
            )
        )
    }

    fun deleteCities(ids: Set<Long>) = viewModelScope.launch {
        dao.deleteByIds(ids)
    }
}