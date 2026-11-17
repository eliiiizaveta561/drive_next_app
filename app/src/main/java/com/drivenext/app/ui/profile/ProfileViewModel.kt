package com.drivenext.app.ui.profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.drivenext.app.R
import com.drivenext.app.data.repository.SupabaseAuthRepository
import com.drivenext.app.data.repository.SupabaseProfileRepository
import com.drivenext.app.domain.model.UserProfile
import com.drivenext.app.domain.repository.AuthRepository
import com.drivenext.app.domain.repository.ProfileRepository
import com.drivenext.app.ui.common.RequestState
import kotlinx.coroutines.launch

class ProfileViewModel(app: Application) : AndroidViewModel(app) {

    private val profileRepository: ProfileRepository = SupabaseProfileRepository(app)
    private val authRepository: AuthRepository = SupabaseAuthRepository(app)

    private val _profile = MutableLiveData<UserProfile>()
    val profile: LiveData<UserProfile> = _profile

    /** Загрузка профиля и выход из аккаунта — индикатор на весь экран. */
    private val _state = MutableLiveData<RequestState>(RequestState.Idle)
    val state: LiveData<RequestState> = _state

    /** Загрузка аватара — индикатор только поверх фото. */
    private val _isAvatarUploading = MutableLiveData(false)
    val isAvatarUploading: LiveData<Boolean> = _isAvatarUploading

    private val _signedOut = MutableLiveData(false)
    val signedOut: LiveData<Boolean> = _signedOut

    init {
        loadProfile()
    }

    fun loadProfile() {
        _state.value = RequestState.Loading
        viewModelScope.launch {
            profileRepository.getProfile().fold(
                onSuccess = { profile ->
                    _profile.value = profile
                    _state.value = RequestState.Idle
                },
                onFailure = ::showError
            )
        }
    }

    fun uploadAvatar(imageUri: Uri) {
        if (_isAvatarUploading.value == true) return
        _isAvatarUploading.value = true
        viewModelScope.launch {
            profileRepository.uploadAvatar(imageUri.toString()).fold(
                onSuccess = { avatarUrl ->
                    _profile.value = _profile.value?.copy(avatarUrl = avatarUrl)
                },
                onFailure = ::showError
            )
            _isAvatarUploading.value = false
        }
    }

    fun signOut() {
        _state.value = RequestState.Loading
        viewModelScope.launch {
            authRepository.signOut()
            _state.value = RequestState.Idle
            _signedOut.value = true
        }
    }

    fun onErrorShown() {
        _state.value = RequestState.Idle
    }

    private fun showError(error: Throwable) {
        _state.value = RequestState.Error(
            error.message ?: getApplication<Application>().getString(R.string.error_unknown)
        )
    }
}
