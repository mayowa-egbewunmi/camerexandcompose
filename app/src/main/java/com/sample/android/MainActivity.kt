package com.sample.android

import VideoScreen
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.android.material.snackbar.Snackbar
import com.sample.android.screens.landing.EntryScreen
import com.sample.android.screens.photo.PhotoScreen
import com.sample.android.screens.photo.PhotoViewModel
import com.sample.android.screens.preview.PreviewScreen
import com.sample.android.shared.utils.FileManager

@OptIn(ExperimentalAnimationApi::class)
class MainActivity : AppCompatActivity() {

    private val fileManager = FileManager(this)

    private val viewModelFactory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PhotoViewModel::class.java)) {
                return PhotoViewModel(fileManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }

    private fun showMessage(message: Int) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
    }

    @Composable
    private fun App() {
        MaterialTheme {
            Scaffold(topBar = {}) {

                val navController = rememberAnimatedNavController()

                AnimatedNavHost(
                    navController = navController,
                    startDestination = ScreenDestinations.Landing.route
                ) {
                    screen(ScreenDestinations.Landing.route) { EntryScreen(navController) }
                    screen(ScreenDestinations.Photo.route) {
                        PhotoScreen(navController, viewModelFactory) {
                            showMessage(it)
                        }
                    }
                    screen(ScreenDestinations.Video.route) { VideoScreen() }
                    screen(ScreenDestinations.Preview.route) { PreviewScreen() }
                }

                BackHandler {
                    navController.popBackStack()
                }
            }
        }
    }

    @ExperimentalAnimationApi
    fun NavGraphBuilder.screen(
        route: String,
        arguments: List<NamedNavArgument> = listOf(),
        content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
    ) {
        val animSpec: FiniteAnimationSpec<IntOffset> = tween(500, easing = FastOutSlowInEasing)

        composable(
            route,
            arguments = arguments,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { screenWidth -> screenWidth },
                    animationSpec = animSpec
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { screenWidth -> -screenWidth },
                    animationSpec = animSpec
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { screenWidth -> -screenWidth },
                    animationSpec = animSpec
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { screenWidth -> screenWidth },
                    animationSpec = animSpec
                )
            },
            content = content
        )
    }
}

fun NavHostController.navigateTo(route: String) = navigate(route) {
    popUpTo(route)
    launchSingleTop = true
}

sealed class ScreenDestinations(val route: String) {
    object Landing : ScreenDestinations("landing")
    object Photo : ScreenDestinations("photo")
    object Video : ScreenDestinations("video")
    object Preview : ScreenDestinations("preview_screen")
}