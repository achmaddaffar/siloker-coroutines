package com.oliver.siloker.data.repository

import android.app.Application
import android.net.Uri
import com.oliver.siloker.data.mapper.toEmployerDto
import com.oliver.siloker.data.mapper.toGetProfileDomain
import com.oliver.siloker.data.mapper.toJobSeekerDto
import com.oliver.siloker.data.network.model.response.BaseResponse
import com.oliver.siloker.data.network.service.UserService
import com.oliver.siloker.data.pref.SiLokerPreference
import com.oliver.siloker.data.util.getResponse
import com.oliver.siloker.domain.error.NetworkError
import com.oliver.siloker.domain.model.request.UpdateEmployerRequest
import com.oliver.siloker.domain.model.request.UpdateJobSeekerRequest
import com.oliver.siloker.domain.model.response.GetProfileResponse
import com.oliver.siloker.domain.repository.UserRepository
import com.oliver.siloker.domain.util.Result
import com.oliver.siloker.domain.util.map
import com.oliver.siloker.domain.util.FileUtil
import com.oliver.siloker.domain.util.onSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class UserRepositoryImpl(
    private val userService: UserService,
    private val preference: SiLokerPreference,
    private val application: Application
) : UserRepository {

    override fun getProfile(): Flow<Result<GetProfileResponse, NetworkError>> =
        flow {
            val response = getResponse { userService.getProfile() }
            response.onSuccess {
                preference.putEmployerId(it.data.employer?.id ?: -1)
                preference.putJobSeekerId(it.data.jobSeeker?.id ?: -1)
            }
            emit(response.map { it.data.toGetProfileDomain() })
        }.flowOn(Dispatchers.IO)

    override fun uploadProfilePicture(uri: Uri): Flow<Result<BaseResponse<Boolean>, NetworkError>> =
        flow {
            val file = FileUtil.uriToFile(uri, application)
            val part = MultipartBody.Part.createFormData(
                "profile_picture",
                file.name,
                file.asRequestBody("image/*".toMediaTypeOrNull())
            )
            val response = getResponse { userService.updateProfilePicture(part) }
            emit(response)
        }.flowOn(Dispatchers.IO)

    override fun updateJobSeeker(request: UpdateJobSeekerRequest): Flow<Result<BaseResponse<Boolean>, NetworkError>> =
        flow {
            val response = getResponse { userService.updateJobSeeker(request.toJobSeekerDto()) }
            emit(response)
        }.flowOn(Dispatchers.IO)

    override fun updateEmployer(request: UpdateEmployerRequest): Flow<Result<BaseResponse<Boolean>, NetworkError>> =
        flow {
            val response = getResponse { userService.updateEmployer(request.toEmployerDto()) }
            emit(response)
        }.flowOn(Dispatchers.IO)

    override fun getEmployerId(): Long = preference.getEmployerId()

    override fun getJobSeekerId(): Long = preference.getJobSeekerId()
}