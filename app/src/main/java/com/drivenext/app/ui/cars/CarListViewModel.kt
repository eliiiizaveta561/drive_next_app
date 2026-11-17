package com.drivenext.app.ui.cars

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.drivenext.app.R
import com.drivenext.app.data.network.NetworkChecker
import com.drivenext.app.data.repository.SupabaseCarRepository
import com.drivenext.app.domain.model.Car
import com.drivenext.app.domain.repository.CarRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Общая логика загрузки списка автомобилей для «Главной» и «Результатов поиска».
 * Наследник говорит только, ЧТО загрузить (requestCars) и какой текст показать при ошибке.
 */
abstract class CarListViewModel(
    app: Application,
    @StringRes private val errorMessageRes: Int
) : AndroidViewModel(app) {

    protected val carRepository: CarRepository = SupabaseCarRepository(app)
    private val networkChecker = NetworkChecker(app)

    private val _state = MutableLiveData<CarsState>(CarsState.Loading)
    val state: LiveData<CarsState> = _state

    /** Текст ошибки от сервера для диалога; null — диалог показывать не нужно. */
    private val _errorDialog = MutableLiveData<String?>()
    val errorDialog: LiveData<String?> = _errorDialog

    private var loadJob: Job? = null

    protected abstract suspend fun requestCars(): Result<List<Car>>

    fun load() {
        loadJob?.cancel()
        if (!networkChecker.isOnline()) {
            _state.value = CarsState.NoConnection
            return
        }

        _state.value = CarsState.Loading
        loadJob = viewModelScope.launch {
            requestCars().fold(
                onSuccess = { cars -> _state.value = CarsState.Content(cars) },
                onFailure = { error -> onLoadFailed(error) }
            )
        }
    }

    fun onErrorDialogShown() {
        _errorDialog.value = null
    }

    /** Экран «Нет подключения» открыт — дальше на этом экране показываем ошибку с кнопкой повтора. */
    fun onNoConnectionShown() {
        _state.value = CarsState.Error(errorMessageRes)
    }

    private fun onLoadFailed(error: Throwable) {
        if (!networkChecker.isOnline()) {
            _state.value = CarsState.NoConnection
            return
        }
        _state.value = CarsState.Error(errorMessageRes)
        _errorDialog.value = error.message ?: getApplication<Application>().getString(R.string.error_unknown)
    }
}
