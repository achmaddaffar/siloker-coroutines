package com.oliver.siloker.presentation.feature.auth.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.oliver.siloker.R
import com.oliver.siloker.presentation.component.FinishActivityValidation
import com.oliver.siloker.presentation.util.ErrorMessageUtil.parseNetworkError
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    snackbarHostState: SnackbarHostState,
    onHomeNavigate: () -> Unit,
    onRegisterNavigate: () -> Unit,
    onBackNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = hiltViewModel<LoginViewModel>()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is LoginEvent.Error -> {
                    snackbarHostState.showSnackbar(
                        message = event.error.parseNetworkError(context),
                        duration = SnackbarDuration.Short
                    )
                }

                LoginEvent.Success -> onHomeNavigate()
            }
        }
    }

    FinishActivityValidation(snackbarHostState) {
        onBackNavigate()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.let_s_go_get_a_job),
            fontSize = 24.sp
        )
        Spacer(Modifier.height(24.dp))
        TextField(
            value = state.phoneNumber,
            onValueChange = { value ->
                if (value.all { it.isDigit() } && value.length <= 16)
                    viewModel.setPhoneNumber(value)
            },
            label = {
                Text(stringResource(R.string.phone_number))
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions {
                focusManager.moveFocus(FocusDirection.Down)
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("phone_number_text_field")
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
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions {
                focusManager.clearFocus()
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
            modifier = Modifier
                .fillMaxWidth()
                .testTag("password_text_field")
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = dropUnlessResumed {
                viewModel.login()
            },
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_login")
        ) {
            if (state.isLoading) CircularProgressIndicator()
            else Text(text = stringResource(R.string.login))
        }
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.don_t_have_an_account_yet)
            )
            TextButton(
                onClick = onRegisterNavigate
            ) {
                Text(stringResource(R.string.register))
            }
        }
    }
}