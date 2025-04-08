package com.shujinko.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.shujinko.app.ui.screen.ImagePickerScreen
import com.shujinko.app.ui.theme.ShujinkoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShujinkoTheme {
                ImagePickerScreen()
            }
        }
    }
}