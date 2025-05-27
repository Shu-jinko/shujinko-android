package com.shujinko.app.ui.screen

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import coil.compose.rememberAsyncImagePainter
import com.shujinko.app.viewmodel.MultiImageViewModel
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiImagePickerScreen(viewModel: MultiImageViewModel = viewModel()) {
    val context = LocalContext.current
    val imageList by viewModel.images.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {}

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        viewModel.addImages(context, uris)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("이미지 정보 보기") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Button(
                onClick = {
                    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        Manifest.permission.READ_MEDIA_IMAGES
                    else
                        Manifest.permission.READ_EXTERNAL_STORAGE

                    permissionLauncher.launch(permission)
                    imagePickerLauncher.launch("image/*")
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("📂 이미지 선택하기")
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (imageList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("선택된 이미지가 없습니다.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(imageList) { image ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(image.uri),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .aspectRatio(1f)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(image.name)
                                    Text("크기: ${image.sizeKb} KB")
                                    Text("촬영일: ${image.dateTaken ?: "정보 없음"}")
                                    Text("위치: ${
                                        if (image.latitude != null && image.longitude != null)
                                            "${image.latitude}, ${image.longitude}"
                                        else "없음"
                                    }")
                                }
                            }
                        }
                    }
                }

                val hasAnyLocation = imageList.any { it.latitude != null && it.longitude != null }
                if (!hasAnyLocation) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "❗ 위치 정보가 없습니다",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = { openAppSettings(context) }) {
                            Text("앱 권한 설정 열기")
                        }
                    }
                }
            }
        }
    }
}

fun openAppSettings(context: android.content.Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}
