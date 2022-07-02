package com.sample.android

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import com.sample.android.screens.entry.EntryScreen

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContent {
            MaterialTheme {
                EntryScreen()
            }
        }
    }
}