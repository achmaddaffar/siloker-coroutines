package com.oliver.siloker.presentation.feature.dashboard.profile

import android.net.Uri
import android.os.Trace
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oliver.siloker.domain.repository.AuthRepository
import com.oliver.siloker.domain.repository.UserRepository
import com.oliver.siloker.domain.util.onError
import com.oliver.siloker.domain.util.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = _state
        .onStart { getProfile() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(15000L),
            ProfileState()
        )

    private val _event = MutableSharedFlow<ProfileEvent>()
    val event = _event.asSharedFlow()

    fun setIsRefreshing(value: Boolean) {
        _state.update { it.copy(isRefreshing = value) }
    }

    fun getProfile() {
        val startTime = System.currentTimeMillis()
        Trace.beginSection("Coroutine_getProfile")
        userRepository
            .getProfile()
            .onStart { _state.update { it.copy(isLoading = true) } }
            .onEach { result ->
                result
                    .onSuccess { response ->
                        _state.update {
                            it.copy(
                                fullName = response.fullName,
                                bio = response.bio,
                                profilePictureUrl = response.profilePictureUrl,
                                jobSeeker = response.jobSeeker,
                                employer = response.employer
                            )
                        }
                    }
                    .onError { _event.emit(ProfileEvent.Error(it)) }
            }
            .onCompletion {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false
                    )
                }
                Trace.endSection()
                Log.e("INGFO", "duration (getProfile): ${System.currentTimeMillis() - startTime}")
            }
            .launchIn(viewModelScope)
    }

    fun uploadProfilePicture(uri: Uri) {
        userRepository
            .uploadProfilePicture(uri)
            .onStart {
                _state.update { it.copy(isLoading = true) }
            }
            .onEach { result ->
                result
                    .onSuccess { getProfile() }
                    .onError { _event.emit(ProfileEvent.Error(it)) }
            }
            .onCompletion {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun logout() {
        authRepository.logout()
    }
}