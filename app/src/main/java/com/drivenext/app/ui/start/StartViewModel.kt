package com.drivenext.app.ui.start

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.drivenext.app.data.local.SessionStorage
import com.drivenext.app.data.network.NetworkChecker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StartViewModel(app: Application) : AndroidViewModel(app) {

    private val networkChecker = NetworkChecker(app)
    private val sessionStorage = SessionStorage(app)

    private val _destination = MutableLiveData<StartDestination>()
    val destination: LiveData<StartDestination> = _destination

    fun chooseDestination(delayMillis: Long = 0) {
        viewModelScope.launch {
            delay(delayMillis)
            _destination.value = when {
                !networkChecker.isOnline() -> StartDestination.NO_CONNECTION
                !sessionStorage.isOnboardingShown -> StartDestination.ONBOARDING
                sessionStorage.hasActiveSession() -> StartDestination.MAIN
                else -> StartDestination.WELCOME
            }
        }
    }
}
