package com.velocoach.app.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.velocoach.app.VeloCoachApp
import com.velocoach.app.data.AthleteProfileEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as VeloCoachApp).repository

    val profile: StateFlow<AthleteProfileEntity?> = repo.observeProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setFtp(watts: Int) {
        viewModelScope.launch { repo.saveFtp(watts) }
    }
}
