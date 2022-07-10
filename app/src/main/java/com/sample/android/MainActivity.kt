package com.sample.android

import VideoScreen
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sample.android.screens.landing.EntryScreen
import com.sample.android.screens.photo.PhotoScreen
import com.sample.android.screens.photo.PhotoViewModel
import com.sample.android.screens.preview.PreviewScreen

class MainActivity : AppCompatActivity() {

    private val viewModelFactory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PhotoViewModel::class.java)) {
                return PhotoViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }

    @Composable
    private fun App() {
        MaterialTheme {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = ScreenDestinations.Landing.route) {
                composable(ScreenDestinations.Landing.route) { EntryScreen() }
                composable(ScreenDestinations.Photo.route) { PhotoScreen(navController, viewModelFactory) }
                composable(ScreenDestinations.Video.route) { VideoScreen() }
                composable(ScreenDestinations.Preview.route) { PreviewScreen() }
            }
        }
    }
}


sealed class ScreenDestinations(val route: String) {
    object Landing : ScreenDestinations("landing")
    object Photo : ScreenDestinations("photo")
    object Video : ScreenDestinations("video")
    object Preview : ScreenDestinations("preview_screen")
}