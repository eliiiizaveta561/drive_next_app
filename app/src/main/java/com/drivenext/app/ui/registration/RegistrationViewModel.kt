package com.drivenext.app.ui.registration

import android.app.Application
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.drivenext.app.R
import com.drivenext.app.data.repository.SupabaseAuthRepository
import com.drivenext.app.domain.model.Gender
import com.drivenext.app.domain.model.RegistrationData
import com.drivenext.app.domain.repository.AuthRepository
import com.drivenext.app.domain.validation.Validator
import com.drivenext.app.ui.common.RequestState
import kotlinx.coroutines.launch

class RegistrationViewModel(app: Application) : AndroidViewModel(app) {

    private val authRepository: AuthRepository = SupabaseAuthRepository(app)

    private var email = ""
    private var password = ""
    private var lastName = ""
    private var firstName = ""
    private var middleName = ""
    private var birthDate = ""
    private var gender: Gender? = null

    private val photos = mutableMapOf<PhotoTarget, Uri>()
    var selectedPhotoTarget: PhotoTarget? = null
    var cameraPhotoUri: Uri? = null

    private val _state = MutableLiveData<RequestState>(RequestState.Idle)
    val state: LiveData<RequestState> = _state

    @StringRes
    fun saveStep1(email: String, password: String, repeatPassword: String, isAgreed: Boolean): Int? {
        if (!Validator.isEmailValid(email)) return R.string.error_email
        if (password.isEmpty()) return R.string.error_required
        if (password != repeatPassword) return R.string.error_passwords
        if (!isAgreed) return R.string.error_agreement

        this.email = email.trim()
        this.password = password
        return null
    }

    fun isStep2Filled(lastName: String, firstName: String, birthDate: String, gender: Gender?): Boolean =
        lastName.isNotBlank() && firstName.isNotBlank() && birthDate.isNotBlank() && gender != null

    @StringRes
    fun saveStep2(lastName: String, firstName: String, middleName: String, birthDate: String, gender: Gender?): Int? {
        if (gender == null || !isStep2Filled(lastName, firstName, birthDate, gender)) return R.string.error_required
        if (!Validator.isBirthDateValid(birthDate)) return R.string.error_birth_date

        this.lastName = lastName.trim()
        this.firstName = firstName.trim()
        this.middleName = middleName.trim()
        this.birthDate = birthDate
        this.gender = gender
        return null
    }

    fun isStep3Filled(licenseNumber: String, issueDate: String): Boolean =
        licenseNumber.isNotBlank() && issueDate.isNotBlank()

    fun savePhoto(uri: Uri): PhotoTarget? {
        val target = selectedPhotoTarget ?: return null
        photos[target] = uri
        return target
    }

    fun savedPhotos(): Map<PhotoTarget, Uri> = photos

    @StringRes
    fun register(licenseNumber: String, issueDate: String): Int? {
        val gender = gender ?: return R.string.error_required
        if (!isStep3Filled(licenseNumber, issueDate)) return R.string.error_required
        if (!Validator.isLicenseNumberValid(licenseNumber)) return R.string.error_license
        if (!Validator.isIssueDateValid(issueDate)) return R.string.error_issue_date
        if (PhotoTarget.LICENSE !in photos || PhotoTarget.PASSPORT !in photos) return R.string.error_photos

        val registration = RegistrationData(
            email = email,
            password = password,
            lastName = lastName,
            firstName = firstName,
            middleName = middleName,
            birthDate = birthDate,
            gender = gender,
            licenseNumber = licenseNumber.trim(),
            licenseIssueDate = issueDate
        )

        _state.value = RequestState.Loading
        viewModelScope.launch {
            val result = authRepository.signUp(registration)
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
