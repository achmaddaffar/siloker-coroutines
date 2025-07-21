package com.oliver.siloker.presentation.feature.job.detail

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.oliver.siloker.R
import com.oliver.siloker.domain.util.DateUtil
import com.oliver.siloker.domain.util.FileUtil
import com.oliver.siloker.presentation.component.LoadingDialog
import com.oliver.siloker.presentation.component.ResultDialog
import com.oliver.siloker.presentation.component.dottedBorder
import com.oliver.siloker.presentation.ui.theme.AppTypography
import com.oliver.siloker.presentation.util.ErrorMessageUtil.parseNetworkError

@Composable
fun JobDetailScreen(
    snackbarHostState: SnackbarHostState,
    onBackNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = hiltViewModel<JobDetailViewModel>()
    val context = LocalContext.current
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) {
        it?.let { viewModel.setCvUri(it) }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val isApplyEnabled by viewModel.isApplyEnabled.collectAsStateWithLifecycle()
    var isUploadSectionVisible by rememberSaveable { mutableStateOf(false) }
    var isSuccessPopUpVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is JobDetailEvent.Error -> {
                    snackbarHostState.showSnackbar(
                        message = event.error.parseNetworkError(context),
                        duration = SnackbarDuration.Short
                    )
                }

                JobDetailEvent.Success -> {
                    isSuccessPopUpVisible = true
                }
            }
        }
    }

    if (state.isLoading) LoadingDialog()
    if (isSuccessPopUpVisible) {
        ResultDialog(
            title = stringResource(R.string.success),
            image = R.drawable.illustration_success,
            description = "Application has been submitted",
            onDismissRequest = {  },
            primaryButtonText = stringResource(R.string.back),
            onPrimaryButtonClick = onBackNavigate
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SubcomposeAsyncImage(
            model = state.jobDetail.imageUrl,
            contentDescription = stringResource(R.string.job_picture),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .aspectRatio(16f / 9f)
                .fillMaxWidth()
                .testTag("job_image")
        )
        Spacer(Modifier.height(20.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = state.jobDetail.title,
                style = AppTypography.headlineSmall
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.description) + ":",
                style = AppTypography.bodySmall
            )
            Text(
                text = state.jobDetail.description,
                style = AppTypography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(
                    R.string.posted_on_s,
                    DateUtil.formatIsoToString(state.jobDetail.createdAt)
                ),
                style = AppTypography.labelMedium
            )
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SubcomposeAsyncImage(
                    model = state.jobDetail.profilePictureUrl,
                    contentDescription = stringResource(R.string.profile_picture),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                )
                Column {
                    Text(
                        text = stringResource(R.string.employer),
                        style = AppTypography.bodyLarge
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = state.jobDetail.fullName,
                        style = AppTypography.bodyMedium
                    )
                    Text(
                        text = state.jobDetail.bio,
                        style = AppTypography.bodySmall
                    )
                    Text(
                        text = stringResource(
                            R.string.s_at_s,
                            state.jobDetail.position,
                            state.jobDetail.companyName
                        ),
                        style = AppTypography.bodySmall
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            AnimatedVisibility(
                visible = isUploadSectionVisible
            ) {
                Box(
                    modifier = Modifier
                        .aspectRatio(16f / 9f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .dottedBorder(
                            color = MaterialTheme.colorScheme.primary,
                            cornerRadius = 16.dp
                        )
                        .clickable {
                            pdfPickerLauncher.launch("application/pdf")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (state.cvUri == Uri.EMPTY) Icons.Default.Add
                            else Icons.Default.FilePresent,
                            contentDescription = stringResource(R.string.upload_cv),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (state.cvUri == Uri.EMPTY) stringResource(R.string.upload_cv)
                            else FileUtil.getFileNameFromUri(context, state.cvUri).toString(),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    if (isUploadSectionVisible) {
                        viewModel.applyJob()
                    } else isUploadSectionVisible = true
                },
                enabled = if (isUploadSectionVisible) isApplyEnabled else true,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.apply))
            }
        }
    }
}