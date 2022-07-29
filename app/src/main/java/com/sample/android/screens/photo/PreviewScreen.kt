package com.sample.android.screens.photo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil.compose.AsyncImage

@Composable
fun PreviewScreen(filePath: String) {
    AsyncImage(model = filePath, modifier = Modifier.fillMaxSize(), contentDescription = "")
}