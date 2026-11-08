package com.drivenext.app.ui.login

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.drivenext.app.R
import com.drivenext.app.data.repository.SupabaseAuthRepository
import com.drivenext.app.domain.repository.AuthRepository
import com.drivenext.app.domain.validation.Validator
import com.drivenext.app.ui.common.RequestState
import kotlinx.coroutines.launch

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    private val authRepository: AuthRepository = SupabaseAuthRepository(app)

    private val _state = MutableLiveData<RequestState>(RequestState.Idle)
    val state: LiveData<RequestState> = _state

    fun isFormFilled(email: String, password: String): Boolean =
        email.isNotBlank() && password.isNotEmpty()

    @StringRes
    fun signIn(email: String, password: String): Int? {
        if (!Validator.isEmailValid(email)) return R.string.error_email

        _state.value = RequestState.Loading
        viewModelScope.launch {
            val result = authRepository.signIn(email.trim(), password)
            _state.value = result.toRequestState()
        }
        return null
    }

    fun onErrorShown() {
        _state.value = RequestState.Idle
    }

    private fun Result<Unit>.toRequestState(): RequestState = fold(
        onSuccess = { RequestState.Success },
        onFailure = { error ->
            RequestState.Error(error.message ?: getApplication<Application>().getString(R.string.error_unknown))
        }
    )
}
