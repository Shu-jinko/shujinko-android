package com.shujinko.app.ui.screen

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shujinko.app.viewmodel.UsageStatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageStatsScreen(viewModel: UsageStatsViewModel = viewModel()) {
    val context = LocalContext.current
    val usageList by viewModel.usageList.collectAsState()

    LaunchedEffect(Unit) {
        if (viewModel.hasUsagePermission(context)) {
            viewModel.loadUsageStats(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📱 앱 사용 시간 요약") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            if (!viewModel.hasUsagePermission(context)) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "앱 사용 기록을 보기 위해\n권한이 필요해요.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        }
                    ) {
                        Text("권한 설정으로 이동")
                    }
                }
            } else {
                if (usageList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("오늘의 앱 사용 기록이 없습니다 💤")
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(usageList) { usage ->
                            UsageStatCard(usage.appName, usage.usageSeconds)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UsageStatCard(appName: String, seconds: Long) {
    val minutes = seconds / 60
    val secs = seconds % 60

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(appName, style = MaterialTheme.typography.titleMedium)
                Text("총 사용 시간", style = MaterialTheme.typography.labelSmall)
            }
            Text(
                text = String.format("%02d:%02d", minutes, secs),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
