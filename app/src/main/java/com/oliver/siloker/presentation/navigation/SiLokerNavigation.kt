package com.oliver.siloker.presentation.navigation

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.oliver.siloker.presentation.feature.auth.login.LoginScreen
import com.oliver.siloker.presentation.feature.auth.register.RegisterScreen
import com.oliver.siloker.presentation.feature.auth.splash.SplashScreen
import com.oliver.siloker.presentation.feature.dashboard.DashboardScreen
import com.oliver.siloker.presentation.feature.dashboard.profile.edit_employer.EditEmployerScreen
import com.oliver.siloker.presentation.feature.dashboard.profile.edit_job_seeker.EditJobSeekerScreen
import com.oliver.siloker.presentation.feature.job.ad.JobAdvertisedListScreen
import com.oliver.siloker.presentation.feature.job.applicant.JobApplicantScreen
import com.oliver.siloker.presentation.feature.job.applicant_detail.JobApplicantDetailScreen
import com.oliver.siloker.presentation.feature.job.application.JobApplicationListScreen
import com.oliver.siloker.presentation.feature.job.detail.JobDetailScreen
import com.oliver.siloker.presentation.feature.job.post.PostJobScreen
import com.oliver.siloker.presentation.navigation.route.AuthRoutes
import com.oliver.siloker.presentation.navigation.route.DashboardRoutes
import com.oliver.siloker.presentation.navigation.route.JobRoutes
import com.oliver.siloker.presentation.util.dropUnlessResumedWithParam

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SiLokerNavigation(
    activity: Activity?,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { _ ->
        NavHost(
            navController = navController,
            startDestination = AuthRoutes.SplashScreen,
            modifier = Modifier
                .semantics {
                    testTagsAsResourceId = true
                }
        ) {
            composable<AuthRoutes.SplashScreen> {
                SplashScreen(
                    modifier = modifier,
                    onLoginNavigate = {
                        navController.navigate(AuthRoutes.LoginScreen) {
                            popUpTo<AuthRoutes.SplashScreen> {
                                inclusive = true
                            }
                        }
                    },
                    onHomeNavigate = {
                        if (activity?.intent?.action == "com.oliver.siloker.PROFILE_ACTION") {
                            navController.navigate(DashboardRoutes.DashboardScreen(2)) {
                                popUpTo<AuthRoutes.SplashScreen> {
                                    inclusive = true
                                }
                            }
                            return@SplashScreen
                        }

                        navController.navigate(DashboardRoutes.DashboardScreen(0)) {
                            popUpTo<AuthRoutes.SplashScreen> {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable<AuthRoutes.LoginScreen> {
                LoginScreen(
                    snackbarHostState = snackbarHostState,
                    modifier = modifier,
                    onHomeNavigate = {
                        if (activity?.intent?.action == "com.oliver.siloker.PROFILE_ACTION") {
                            navController.navigate(DashboardRoutes.DashboardScreen(2)) {
                                popUpTo<AuthRoutes.SplashScreen> {
                                    inclusive = true
                                }
                            }
                            return@LoginScreen
                        }

                        navController.navigate(DashboardRoutes.DashboardScreen(0)) {
                            popUpTo<AuthRoutes.LoginScreen> {
                                inclusive = true
                            }
                        }
                    },
                    onRegisterNavigate = dropUnlessResumed {
                        navController.navigate(AuthRoutes.RegisterScreen)
                    },
                    onBackNavigate = {
                        activity?.finish()
                    }
                )
            }

            composable<AuthRoutes.RegisterScreen> {
                RegisterScreen(
                    snackbarHostState = snackbarHostState,
                    modifier = modifier,
                    onLoginNavigate = dropUnlessResumed {
                        navController.navigate(AuthRoutes.LoginScreen) {
                            popUpTo<AuthRoutes.LoginScreen> {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable<DashboardRoutes.DashboardScreen> {
                val args = it.toRoute<DashboardRoutes.DashboardScreen>()

                DashboardScreen(
                    snackbarHostState = snackbarHostState,
                    initialContentIndex = args.contentIndex,
                    onJobDetailNavigate = dropUnlessResumedWithParam(
                        navController
                    ) {
                        navController.navigate(JobRoutes.JobDetailScreen(it))
                    },
                    onJobAdvertisedNavigate = dropUnlessResumedWithParam(navController) {
                        navController.navigate(JobRoutes.JobApplicantsScreen(it))
                    },
                    onPostJobNavigate = dropUnlessResumed {
                        navController.navigate(JobRoutes.PostJobScreen)
                    },
                    onEditJobSeekerNavigate = dropUnlessResumed {
                        navController.navigate(DashboardRoutes.EditJobSeekerScreen)
                    },
                    onEditEmployerNavigate = dropUnlessResumed {
                        navController.navigate(DashboardRoutes.EditEmployerScreen)
                    },
                    onLogoutNavigate = dropUnlessResumed {
                        navController.navigate(AuthRoutes.LoginScreen) {
                            popUpTo<AuthRoutes.LoginScreen> {
                                inclusive = true
                            }
                        }
                    },
                    onMoreApplicantsNavigate = dropUnlessResumed {
                        navController.navigate(JobRoutes.JobApplicationListScreen)
                    },
                    onMoreJobsAdvertisedNavigate = dropUnlessResumed {
                        navController.navigate(JobRoutes.JobAdvertisedListScreen)
                    },
                    modifier = modifier
                )
            }

            composable<DashboardRoutes.EditJobSeekerScreen> {
                EditJobSeekerScreen(
                    snackbarHostState = snackbarHostState,
                    onBackNavigate = dropUnlessResumed {
                        navController.navigateUp()
                    },
                    modifier = modifier
                )
            }

            composable<DashboardRoutes.EditEmployerScreen> {
                EditEmployerScreen(
                    snackbarHostState = snackbarHostState,
                    onBackNavigate = dropUnlessResumed {
                        navController.navigateUp()
                    },
                    modifier = modifier
                )
            }

            composable<JobRoutes.PostJobScreen> {
                PostJobScreen(
                    snackbarHostState = snackbarHostState,
                    onBackNavigate = dropUnlessResumed {
                        navController.navigateUp()
                    },
                    modifier = modifier
                )
            }

            composable<JobRoutes.JobDetailScreen> {
                JobDetailScreen(
                    snackbarHostState = snackbarHostState,
                    onBackNavigate = dropUnlessResumed {
                        navController.navigateUp()
                    },
                    modifier = modifier
                )
            }

            composable<JobRoutes.JobApplicationListScreen> {
                JobApplicationListScreen(
                    snackbarHostState = snackbarHostState,
                    onJobAdNavigate = dropUnlessResumedWithParam(navController){
                        navController.navigate(JobRoutes.JobDetailScreen(it))
                    },
                    modifier = modifier
                )
            }

            composable<JobRoutes.JobAdvertisedListScreen> {
                JobAdvertisedListScreen(
                    snackbarHostState = snackbarHostState,
                    onJobAdClick = dropUnlessResumedWithParam(navController) {
                        navController.navigate(JobRoutes.JobApplicantsScreen(it))
                    },
                    modifier = modifier
                )
            }

            composable<JobRoutes.JobApplicantsScreen> {
                JobApplicantScreen(
                    snackbarHostState = snackbarHostState,
                    onApplicantDetailNavigate = dropUnlessResumedWithParam(navController) {
                        navController.navigate(JobRoutes.JobApplicantDetailScreen(it))
                    },
                    modifier = modifier
                )
            }

            composable<JobRoutes.JobApplicantDetailScreen> {
                JobApplicantDetailScreen(
                    snackbarHostState = snackbarHostState,
                    modifier = modifier
                )
            }
        }
    }
}