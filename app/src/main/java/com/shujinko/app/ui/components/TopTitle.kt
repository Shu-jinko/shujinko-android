package com.shujinko.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopTitle(
    title: String,
    bottomBar: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val bottomBarHeight = 130.dp

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
        },
        bottomBar = {
            if (bottomBar != null) {
                Surface(
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(bottomBarHeight) // ✅ 고정 높이
                        .navigationBarsPadding() // 시스템 바 피하기 (선택)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    bottomBar()
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                top = 32.dp,
                bottom = innerPadding.calculateBottomPadding() + 80.dp
            ),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            item {
                Column {
                    content()
                }
            }
        }
    }
}
