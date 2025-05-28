package com.shujinko.app

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.shujinko.app.navigation.ShujinkoNavGraph
import com.shujinko.app.ui.theme.ShujinkoTheme
import com.shujinko.app.utils.TokenStore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContent {
            ShujinkoTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val context = LocalContext.current
                    val navController = rememberNavController()
                    val token by TokenStore.getAccessToken(context).collectAsState(initial = "")

                    ShujinkoNavGraph(
                        navController = navController,
                        token = token.toString()
                    )
                }
            }
        }
    }
}