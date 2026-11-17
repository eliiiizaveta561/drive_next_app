package com.drivenext.app.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.drivenext.app.R
import com.drivenext.app.data.repository.SupabaseProfileRepository
import com.drivenext.app.domain.model.UserProfile
import com.drivenext.app.domain.repository.ProfileRepository
import com.drivenext.app.ui.common.RequestState
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val profileRepository: ProfileRepository = SupabaseProfileRepository(app)

    private val _profile = MutableLiveData<UserProfile>()
    val profile: LiveData<UserProfile> = _profile

    private val _state = MutableLiveData<RequestState>(RequestState.Idle)
    val state: LiveData<RequestState> = _state

    /** Вызывается при каждом показе экрана — после смены аватара в «Профиле» данные обновятся. */
    fun loadProfile() {
        if (_state.value == RequestState.Loading) return
        _state.value = RequestState.Loading
        viewModelScope.launch {
            profileRepository.getProfile().fold(
                onSuccess = { profile ->
                    _profile.value = profile
                    _state.value = RequestState.Idle
                },
                onFailure = { error ->
                    _state.value = RequestState.Error(
                        error.message ?: getApplication<Application>().getString(R.string.error_unknown)
                    )
                }
            )
        }
    }

    fun onErrorShown() {
        _state.value = RequestState.Idle
    }
}
