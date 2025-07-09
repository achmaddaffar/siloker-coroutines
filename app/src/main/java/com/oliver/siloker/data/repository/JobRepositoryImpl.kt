package com.oliver.siloker.data.repository

import android.app.Application
import android.net.Uri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.oliver.siloker.data.mapper.toApplicantsLatestDomain
import com.oliver.siloker.data.mapper.toDomain
import com.oliver.siloker.data.mapper.toJobDetailDomain
import com.oliver.siloker.data.mapper.toJobLatestDomain
import com.oliver.siloker.data.network.model.response.BaseResponse
import com.oliver.siloker.data.network.paging.GetApplicantsPagingSource
import com.oliver.siloker.data.network.paging.GetJobPagingSource
import com.oliver.siloker.data.network.service.JobService
import com.oliver.siloker.data.pref.SiLokerPreference
import com.oliver.siloker.data.util.DownloadUtil
import com.oliver.siloker.data.util.getResponse
import com.oliver.siloker.domain.error.NetworkError
import com.oliver.siloker.domain.model.response.ApplicantsResponseItem
import com.oliver.siloker.domain.model.response.GetLatestApplicationResponse
import com.oliver.siloker.domain.model.response.GetLatestJobResponse
import com.oliver.siloker.domain.model.response.JobAdResponseItem
import com.oliver.siloker.domain.model.response.JobDetailResponse
import com.oliver.siloker.domain.repository.JobRepository
import com.oliver.siloker.domain.util.FileUtil
import com.oliver.siloker.domain.util.Result
import com.oliver.siloker.domain.util.asEmptyDataResult
import com.oliver.siloker.domain.util.map
import com.oliver.siloker.domain.util.onSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class JobRepositoryImpl(
    private val jobService: JobService,
    private val preference: SiLokerPreference,
    private val application: Application
) : JobRepository {

    override fun getJobs(
        query: String,
        isOwner: Boolean
    ): Flow<PagingData<JobAdResponseItem>> = Pager(
        PagingConfig(GetJobPagingSource.PAGE_SIZE)
    ) {
        GetJobPagingSource { page, size ->
            jobService.getJobs(
                query = query,
                employerId = if (isOwner) preference.getEmployerId() else null,
                page = page,
                size = size
            )
        }
    }
        .flow
        .flowOn(Dispatchers.IO)

    override fun getLatestJobs(): Flow<Result<GetLatestJobResponse, NetworkError>> =
        flow {
            val response = getResponse {
                jobService.getJobs(
                    query = "",
                    employerId = preference.getEmployerId(),
                    page = 1,
                    size = 5
                )
            }
            emit(response.map { it.data.toJobLatestDomain() })
        }.flowOn(Dispatchers.IO)

    override fun getJobDetail(
        jobId: Long
    ): Flow<Result<JobDetailResponse, NetworkError>> =
        flow {
            val response = getResponse { jobService.getJobDetail(jobId) }
            emit(response.map { it.data.toJobDetailDomain() })
        }.flowOn(Dispatchers.IO)

    override fun postJob(
        uri: Uri,
        title: String,
        description: String
    ): Flow<Result<BaseResponse<Boolean>, NetworkError>> = flow {
        val file = FileUtil.uriToFile(uri, application)
        val requestImageFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val imageMultipart = MultipartBody.Part.createFormData(
            "image",
            file.name,
            requestImageFile
        )

        val response = getResponse {
            jobService.postJob(
                title = title.toRequestBody("text/plain".toMediaTypeOrNull()),
                description = description.toRequestBody("text/plain".toMediaTypeOrNull()),
                image = imageMultipart
            )
        }

        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun applyJob(
        jobId: Long,
        cv: Uri
    ): Flow<Result<BaseResponse<Boolean>, NetworkError>> =
        flow {
            val file = FileUtil.uriToFile(cv, application)
            val requestCvFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
            val cvMultipart = MultipartBody.Part.createFormData(
                "cv",
                file.name,
                requestCvFile
            )

            val response = getResponse {
                jobService.applyJob(
                    jobId = jobId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    cv = cvMultipart
                )
            }
            emit(response)
        }.flowOn(Dispatchers.IO)

    override fun getApplicants(
        jobId: Long?
    ): Flow<PagingData<ApplicantsResponseItem>> = Pager(
        PagingConfig(GetApplicantsPagingSource.PAGE_SIZE)
    ) {
        GetApplicantsPagingSource { page, size ->
            jobService.getApplicants(jobId, page, size)
        }
    }
        .flow
        .flowOn(Dispatchers.IO)

    override fun getApplicant(applicantId: Long): Flow<Result<ApplicantsResponseItem, NetworkError>> =
        flow {
            val response = getResponse { jobService.getApplicant(applicantId) }
            emit(response.map { it.data.toDomain() })
        }.flowOn(Dispatchers.IO)

    override fun getLatestApplication(): Flow<Result<GetLatestApplicationResponse, NetworkError>> =
        flow {
            val response = getResponse {
                jobService.getApplicants(
                    jobId = null,
                    page = 1,
                    size = 5
                )
            }
            emit(response.map { it.data.toApplicantsLatestDomain() })
        }.flowOn(Dispatchers.IO)

    override fun downloadCv(cvUrl: String): Flow<Result<Unit, NetworkError>> =
        flow {
            val response = getResponse { jobService.downloadCv(cvUrl) }
            response.onSuccess { body ->
                DownloadUtil.saveFileToPublicDownloads(
                    context = application,
                    fileName = cvUrl.split("/").last(),
                    mimeType = "application/pdf",
                    body = body
                )
            }
            emit(response.asEmptyDataResult())
        }.flowOn(Dispatchers.IO)

    override fun acceptApplicant(applicantId: Long): Flow<Result<ApplicantsResponseItem, NetworkError>> =
        flow {
            val response = getResponse { jobService.acceptApplicant(applicantId) }
            emit(response.map { it.data.toDomain() })
        }.flowOn(Dispatchers.IO)

    override fun rejectApplicant(applicantId: Long): Flow<Result<ApplicantsResponseItem, NetworkError>> =
        flow {
            val response = getResponse { jobService.rejectApplicant(applicantId) }
            emit(response.map { it.data.toDomain() })
        }.flowOn(Dispatchers.IO)
}