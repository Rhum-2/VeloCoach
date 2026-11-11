package com.velocoach.app.ui.ride

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.velocoach.app.VeloCoachApp
import com.velocoach.app.data.RideEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class RideViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as VeloCoachApp).repository

    val rides: StateFlow<List<RideEntity>> = repo.observeRides()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
