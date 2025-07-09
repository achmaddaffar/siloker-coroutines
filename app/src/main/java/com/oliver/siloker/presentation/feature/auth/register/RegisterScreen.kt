package com.oliver.siloker.presentation.feature.auth.register

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.oliver.siloker.R
import com.oliver.siloker.domain.error.NetworkError
import com.oliver.siloker.domain.error.auth.RegisterError
import com.oliver.siloker.presentation.component.LoadingDialog
import com.oliver.siloker.presentation.component.ResultDialog
import com.oliver.siloker.presentation.util.ErrorMessageUtil.parseNetworkError
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RegisterScreen(
    snackbarHostState: SnackbarHostState,
    onLoginNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = hiltViewModel<RegisterViewModel>()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) {
        it?.let {
            viewModel.setProfilePictureUri(it)
        }
    }

    var isSuccessPopUpVisible by rememberSaveable { mutableStateOf(false) }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isRepeatPasswordVisible by rememberSaveable { mutableStateOf(false) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isRegisterEnabled by viewModel.isRegisterEnabled.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is RegisterEvent.Error -> {
                    if (event.error is RegisterError)
                        snackbarHostState.showSnackbar(
                            message = when (event.error) {
                                RegisterError.PASSWORD_NOT_SAME -> context.getString(R.string.passwords_are_not_the_same)
                                RegisterError.TOO_SHORT -> context.getString(R.string.password_is_too_short)
                            },
                            duration = SnackbarDuration.Short
                        )
                    else
                        snackbarHostState.showSnackbar(
                            message = (event.error as NetworkError).parseNetworkError(context),
                            duration = SnackbarDuration.Short
                        )
                }

                RegisterEvent.Success -> isSuccessPopUpVisible = true
            }
        }
    }

    if (state.isLoading) LoadingDialog()
    if (isSuccessPopUpVisible)
        ResultDialog(
            title = stringResource(R.string.success),
            image = R.drawable.illustration_success,
            description = stringResource(R.string.you_have_successfully_registered_a_user),
            onDismissRequest = {},
            primaryButtonText = stringResource(R.string.go_to_login),
            onPrimaryButtonClick = onLoginNavigate
        )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.sign_in_and_explore_siloker),
            fontSize = 24.sp
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
                model = state.profilePictureUri,
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
        TextField(
            value = state.fullName,
            onValueChange = viewModel::setFullName,
            label = {
                Text(stringResource(R.string.full_name))
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions {
                focusManager.moveFocus(FocusDirection.Down)
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        TextField(
            value = state.phoneNumber,
            onValueChange = viewModel::setPhoneNumber,
            label = {
                Text(stringResource(R.string.phone_number))
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions {
                focusManager.moveFocus(FocusDirection.Down)
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        TextField(
            value = state.password,
            onValueChange = viewModel::setPassword,
            label = {
                Text(stringResource(R.string.password))
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions {
                focusManager.moveFocus(FocusDirection.Down)
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            singleLine = true,
            trailingIcon = {
                IconButton(
                    onClick = {
                        isPasswordVisible = !isPasswordVisible
                    }
                ) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Visibility
                        else Icons.Default.VisibilityOff,
                        contentDescription = stringResource(R.string.password)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        TextField(
            value = state.repeatPassword,
            onValueChange = viewModel::setRepeatPassword,
            label = {
                Text(stringResource(R.string.repeat_password))
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions {
                focusManager.moveFocus(FocusDirection.Down)
            },
            visualTransformation = if (isRepeatPasswordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            singleLine = true,
            trailingIcon = {
                IconButton(
                    onClick = {
                        isRepeatPasswordVisible = !isRepeatPasswordVisible
                    }
                ) {
                    Icon(
                        imageVector = if (isRepeatPasswordVisible) Icons.Default.Visibility
                        else Icons.Default.VisibilityOff,
                        contentDescription = stringResource(R.string.password)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.bio,
            onValueChange = viewModel::setBio,
            label = { Text(stringResource(R.string.bio)) },
            maxLines = 10,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions {
                focusManager.clearFocus()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = viewModel::register,
            enabled = !state.isLoading && isRegisterEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isLoading) CircularProgressIndicator()
            else Text(stringResource(R.string.register))
        }
    }
}