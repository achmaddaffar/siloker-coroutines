package com.oliver.siloker.presentation.feature.job.post

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Trace
import android.util.Log
import androidx.annotation.RawRes
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oliver.siloker.R
import com.oliver.siloker.domain.repository.JobRepository
import com.oliver.siloker.domain.util.onError
import com.oliver.siloker.domain.util.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class PostJobViewModel @Inject constructor(
    private val jobRepository: JobRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PostJobState())
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<PostJobEvent>()
    val event = _event.asSharedFlow()

//    val isPostEnabled = _state
//        .map {
//            !it.isLoading && it.title.isNotEmpty() && it.description.isNotEmpty() && it.selectedImageUri != Uri.EMPTY
//        }
//        .stateIn(
//            viewModelScope,
//            SharingStarted.WhileSubscribed(15000L),
//            false
//        )
    val isPostEnabled = _state
        .map {
            // FOR TESTING
            true
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(15000L),
            true
        )

    fun setImageUri(uri: Uri) {
        _state.update { it.copy(selectedImageUri = uri) }
    }

    fun setTitle(title: String) {
        _state.update { it.copy(title = title) }
    }

    fun setDescription(desc: String) {
        _state.update { it.copy(description = desc) }
    }
//
//    fun postJob() {
//        Trace.beginSection("Coroutines_postJob")
//        val timeStart = System.currentTimeMillis()
//        jobRepository
//            .postJob(
//                uri = _state.value.selectedImageUri,
//                title = _state.value.title,
//                description = _state.value.description
//            )
//            .onStart { _state.update { it.copy(isLoading = true) } }
//            .onEach { result ->
//                result
//                    .onSuccess {
//                        Trace.endSection()
//                        Log.e("INGFO", "duration: ${System.currentTimeMillis() - timeStart}")
//                        _event.emit(PostJobEvent.Success)
//                    }
//                    .onError { _event.emit(PostJobEvent.Error(it)) }
//            }
//            .onCompletion {
//                _state.update { it.copy(isLoading = false) }
////                Trace.endSection()
////                Log.e("INGFO", "duration: ${System.currentTimeMillis() - timeStart}")
//            }
//            .launchIn(viewModelScope)
//    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun postJob(context: Context) {
        Trace.beginAsyncSection("Coroutines_postJob", 111)
        jobRepository
            .postJob(
                uri = getUriFromRawResource(context, R.raw.post_job_picture_benchmark),
                title = _state.value.title,
                description = _state.value.description,
            )
            .onStart { _state.update { it.copy(isLoading = true) } }
            .onEach { result ->
                result
                    .onSuccess {
                        Trace.endAsyncSection("Coroutines_postJob", 111)
                        _event.emit(PostJobEvent.Success)
                    }
                    .onError { _event.emit(PostJobEvent.Error(it)) }
            }
            .onCompletion {
                _state.update { it.copy(isLoading = false) }
//                Trace.endSection()
//                Log.e("INGFO", "duration: ${System.currentTimeMillis() - timeStart}")
            }
            .launchIn(viewModelScope)
    }

    fun getUriFromRawResource(context: Context, @RawRes resourceId: Int): Uri {
        val inputStream = context.resources.openRawResource(resourceId)
        val fileName = context.resources.getResourceEntryName(resourceId)
        val destinationFile = File(context.cacheDir, "$fileName.jpg")
        FileOutputStream(destinationFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        val authority = "${context.packageName}.provider"
        return FileProvider.getUriForFile(context, authority, destinationFile)
    }
}