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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("📸 여러 장 이미지 선택 테스트", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            imagePickerLauncher.launch("image/*")
        }) {
            Text("이미지 선택하기")
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn {
            items(imageList) { image ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(image.uri),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("🖼 ${image.name}")
                        Text("📏 ${image.sizeKb} KB")
                        Text("📅 ${image.dateTaken ?: "촬영 시각 없음"}")
                        Text("📍 ${if (image.latitude != null && image.longitude != null) "${image.latitude}, ${image.longitude}" else "위치 없음"}")
                    }
                }
            }
        }

        val hasAnyLocation = imageList.any { it.latitude != null && it.longitude != null }

        if (!hasAnyLocation && imageList.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "❗사진 위치 정보를 불러올 수 없어요.\n앱 설정에서 권한을 허용해야 할 수 있어요.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { openAppSettings(context) }) {
                Text("앱 설정으로 이동하기")
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
