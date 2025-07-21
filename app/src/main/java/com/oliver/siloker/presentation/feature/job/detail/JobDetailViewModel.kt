package com.oliver.siloker.presentation.feature.job.detail

import android.net.Uri
import android.os.Build
import android.os.Trace
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oliver.siloker.domain.repository.JobRepository
import com.oliver.siloker.domain.util.onError
import com.oliver.siloker.domain.util.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class JobDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val jobRepository: JobRepository
) : ViewModel() {

    private val jobId: Long = checkNotNull(savedStateHandle["jobId"])

    private val _state = MutableStateFlow(JobDetailState())
    val state = _state
        .onStart { getJobDetail() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(15000L),
            JobDetailState()
        )

    private val _event = MutableSharedFlow<JobDetailEvent>()
    val event = _event.asSharedFlow()

    val isApplyEnabled = _state
        .map {
            it.jobDetail.isApplicable && !it.isLoading && it.cvUri != Uri.EMPTY
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(15000L),
            false
        )

    fun setCvUri(value: Uri) {
        _state.update { it.copy(cvUri = value) }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getJobDetail() {
        Trace.beginAsyncSection("Coroutine_getJobDetail", 222)
        jobRepository
            .getJobDetail(jobId)
            .onStart { _state.update { it.copy(isLoading = true) } }
            .onEach { result ->
                result
                    .onSuccess { response -> _state.update { it.copy(jobDetail = response) } }
                    .onError { _event.emit(JobDetailEvent.Error(it)) }
            }
            .onCompletion {
                _state.update { it.copy(isLoading = false) }
                Trace.endAsyncSection("Coroutine_getJobDetail", 222)
            }
            .launchIn(viewModelScope)
    }

    fun applyJob() {
        jobRepository
            .applyJob(jobId, _state.value.cvUri)
            .onStart { _state.update { it.copy(isLoading = true) } }
            .onEach { result ->
                result
                    .onSuccess { _event.emit(JobDetailEvent.Success) }
                    .onError { _event.emit(JobDetailEvent.Error(it)) }
            }
            .onCompletion { _state.update { it.copy(isLoading = false) } }
            .launchIn(viewModelScope)
    }
}