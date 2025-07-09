package com.oliver.siloker.presentation.feature.dashboard.home

import android.os.Trace
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.oliver.siloker.domain.repository.JobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel @Inject constructor(
    private val jobRepository: JobRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    private val _pagingError = MutableSharedFlow<Throwable>()
    val pagingError = _pagingError.asSharedFlow()

    val jobs = state
        .map { it.query }
        .distinctUntilChanged()
        .flatMapLatest { query ->
            delay(500)
            jobRepository.getJobs(query).cachedIn(viewModelScope)
        }
        .catch { _pagingError.emit(it) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(15000L),
            PagingData.empty()
        )

    fun setQuery(value: String) {
        _state.update { it.copy(query = value) }
    }
}