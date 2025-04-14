package com.shujinko.app.ui.screen

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shujinko.app.viewmodel.UsageStatsViewModel

@Composable
fun UsageStatsScreen(viewModel: UsageStatsViewModel = viewModel()) {
    val context = LocalContext.current
    val usageList by viewModel.usageList.collectAsState()

    LaunchedEffect(Unit) {
        if (viewModel.hasUsagePermission(context)) {
            viewModel.loadUsageStats(context)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("📱 오늘의 앱 사용 기록", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        if (!viewModel.hasUsagePermission(context)) {
            Text("⚠️ 앱 사용 기록 권한이 필요합니다.")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }) {
                Text("권한 설정하러 가기")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(usageList) { usage ->
                    UsageStatCard(usage.appName, usage.usageSeconds)
                }
            }
        }
    }
}

@Composable
fun UsageStatCard(appName: String, seconds: Long) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("앱: $appName", style = MaterialTheme.typography.bodyLarge)
            Text("사용 시간: ${seconds / 60}분 ${seconds % 60}초", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
