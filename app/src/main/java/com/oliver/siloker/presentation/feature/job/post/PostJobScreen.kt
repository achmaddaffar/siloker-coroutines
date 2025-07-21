package com.oliver.siloker.presentation.feature.job.post

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.oliver.siloker.R
import com.oliver.siloker.presentation.component.LoadingDialog
import com.oliver.siloker.presentation.ui.theme.AppTypography
import com.oliver.siloker.presentation.util.ErrorMessageUtil.parseNetworkError

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun PostJobScreen(
    snackbarHostState: SnackbarHostState,
    onBackNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = hiltViewModel<PostJobViewModel>()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) {
        it?.let {
            viewModel.setImageUri(it)
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val isPostEnabled by viewModel.isPostEnabled.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is PostJobEvent.Error -> {
                    snackbarHostState.showSnackbar(
                        message = event.error.parseNetworkError(context),
                        duration = SnackbarDuration.Short
                    )
                }

                PostJobEvent.Success -> onBackNavigate()
            }
        }
    }

    if (state.isLoading) LoadingDialog()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.let_s_post_your_job_advertisement),
            style = AppTypography.headlineMedium,
            modifier = Modifier.testTag("post_job_title")
        )
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .aspectRatio(16f / 9f)
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                .clickable {
                    galleryLauncher.launch("image/*")
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_picture),
                tint = MaterialTheme.colorScheme.primary
            )
            SubcomposeAsyncImage(
                model = state.selectedImageUri,
                contentDescription = stringResource(R.string.selected_image),
                loading = {
                    Box(
                        modifier = Modifier
                            .aspectRatio(16f / 9f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                },
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.title,
            onValueChange = viewModel::setTitle,
            label = { Text(stringResource(R.string.title)) },
            placeholder = {
                Text(stringResource(R.string.enter_job_title))
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions {
                focusManager.moveFocus(FocusDirection.Down)
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("title_text_field")
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = state.description,
            onValueChange = viewModel::setDescription,
            label = { Text(stringResource(R.string.description)) },
            placeholder = {
                Text(stringResource(R.string.enter_job_description))
            },
            maxLines = 10,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .testTag("description_text_field")
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.postJob(context)
            },
            enabled = isPostEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_post")
        ) {
            if (state.isLoading) CircularProgressIndicator()
            else Text(stringResource(R.string.post))
        }
    }
}