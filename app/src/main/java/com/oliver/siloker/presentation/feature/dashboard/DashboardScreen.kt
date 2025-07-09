package com.oliver.siloker.presentation.feature.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.oliver.siloker.R
import com.oliver.siloker.presentation.feature.dashboard.component.SiLokerBottomNavBar
import com.oliver.siloker.presentation.feature.dashboard.history.HistoryContent
import com.oliver.siloker.presentation.feature.dashboard.home.HomeContent
import com.oliver.siloker.presentation.feature.dashboard.profile.ProfileContent

@Composable
fun DashboardScreen(
    snackbarHostState: SnackbarHostState,
    initialContentIndex: Int,
    onJobDetailNavigate: (Long) -> Unit,
    onJobAdvertisedNavigate: (Long) -> Unit,
    onPostJobNavigate: () -> Unit,
    onEditJobSeekerNavigate: () -> Unit,
    onEditEmployerNavigate: () -> Unit,
    onMoreApplicantsNavigate: () -> Unit,
    onMoreJobsAdvertisedNavigate: () -> Unit,
    onLogoutNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedContentIndex by rememberSaveable { mutableIntStateOf(initialContentIndex) }
    var selectedContentIndexBeforeTransition by rememberSaveable { mutableIntStateOf(initialContentIndex) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            SiLokerBottomNavBar(
                selectedContentIndex = selectedContentIndex
            ) {
                selectedContentIndex = it
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = selectedContentIndex == 0,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingActionButton(
                    onClick = onPostJobNavigate,
                    modifier = Modifier
                        .testTag("fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.post_job_ad)
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedContentIndex,
            transitionSpec = if (selectedContentIndexBeforeTransition < selectedContentIndex) {
                selectedContentIndexBeforeTransition = selectedContentIndex
                {
                    ContentTransform(
                        targetContentEnter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                        initialContentExit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                    ).using(SizeTransform(clip = false))
                }
            } else {
                selectedContentIndexBeforeTransition = selectedContentIndex
                {
                    ContentTransform(
                        targetContentEnter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                        initialContentExit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                    ).using(SizeTransform(clip = false))
                }
            },
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) { tabIndex ->
            when (tabIndex) {
                0 -> HomeContent(
                    snackbarHostState = snackbarHostState,
                    onJobAdClick = onJobDetailNavigate,
                    modifier = Modifier.fillMaxSize()
                )

                1 -> HistoryContent(
                    snackbarHostState = snackbarHostState,
                    onMoreApplicantsNavigate = onMoreApplicantsNavigate,
                    onMoreJobsAdvertisedNavigate = onMoreJobsAdvertisedNavigate,
                    onJobApplicationClick = onJobDetailNavigate,
                    onJobAdvertisedClick = onJobAdvertisedNavigate,
                    modifier = Modifier.fillMaxSize(),
                )

                2 -> ProfileContent(
                    snackbarHostState = snackbarHostState,
                    onEditEmployerNavigate = onEditEmployerNavigate,
                    onEditJobSeekerNavigate = onEditJobSeekerNavigate,
                    onLogoutNavigate = onLogoutNavigate,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}