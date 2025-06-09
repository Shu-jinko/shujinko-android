package com.shujinko.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shujinko.app.ui.theme.PrimaryPurple
import com.shujinko.app.ui.theme.TextPrimary
import com.shujinko.app.ui.theme.Pretendard

@Composable
fun S_Button(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryPurple,
            contentColor = TextPrimary
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            fontFamily = Pretendard,
            fontSize = 16.sp
        )
    }
}
