package com.noemi.streamer.util

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import com.noemi.streamer.ui.theme.Typography

@Composable
fun CustomSnackBar(
    snackBarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = snackBarHostState,
        snackbar = { data ->
            Snackbar(
                modifier = modifier.padding(16.dp),
                content = {
                    Text(
                        text = data.message,
                        style = Typography.bodyMedium,
                        color = Color.White
                    )
                },
                contentColor = Color.White,
                backgroundColor = Color.Red,
                elevation = 12.dp
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(Alignment.CenterVertically)
    )
}