package com.shujinko.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.shujinko.app.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shujinko 앱 정보 추출 데모") },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FourCutButton(
                    label = "사진 정보 불러오기",
                    onClick = { navController.navigate(Routes.IMAGE_PICKER) },
                    modifier = Modifier.weight(1f)
                )
                FourCutButton(
                    label = "앱 사용 기록 불러오기",
                    onClick = { navController.navigate(Routes.USAGE_STATS) },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FourCutButton(
                    label = "3번",
                    onClick = { /* TODO */ },
                    modifier = Modifier.weight(1f)
                )
                FourCutButton(
                    label = "4번",
                    onClick = { /* TODO */ },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun FourCutButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxHeight()
    ) {
        Text(label)
    }
}
