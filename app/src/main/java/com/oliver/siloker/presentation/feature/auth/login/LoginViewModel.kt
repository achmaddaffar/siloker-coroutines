package com.oliver.siloker.presentation.feature.auth.login

import android.os.Trace
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oliver.siloker.domain.model.request.LoginRequest
import com.oliver.siloker.domain.repository.AuthRepository
import com.oliver.siloker.domain.util.onError
import com.oliver.siloker.domain.util.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<LoginEvent>()
    val event = _event.asSharedFlow()

    fun setPhoneNumber(
        value: String
    ) {
        _state.value = _state.value.copy(
            phoneNumber = value
        )
    }

    fun setPassword(
        value: String
    ) {
        _state.value = _state.value.copy(
            password = value
        )
    }

    fun login() {
        val request = LoginRequest(
            phoneNumber = _state.value.phoneNumber,
            password = _state.value.password
        )

        Trace.beginSection("Coroutines_Login")
        authRepository
            .login(request)
            .onStart { _state.update { it.copy(isLoading = true) } }
            .onEach { result ->
                result
                    .onSuccess {
                        Trace.endSection()
                        _event.emit(LoginEvent.Success)
                    }
                    .onError { _event.emit(LoginEvent.Error(it)) }
            }
            .onCompletion {
                _state.update { it.copy(isLoading = false) }
                Trace.endSection()
            }
            .launchIn(viewModelScope)
    }
}