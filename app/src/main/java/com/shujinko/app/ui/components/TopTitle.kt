package com.shujinko.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopTitle(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            content()
        }
    }
}

