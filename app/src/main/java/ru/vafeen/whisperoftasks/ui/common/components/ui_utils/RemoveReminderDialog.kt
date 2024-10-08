package ru.vafeen.whisperoftasks.ui.common.components.ui_utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.vafeen.whisperoftasks.ui.theme.Theme

@Composable
fun RemoveReminderDialog(
    onDismissRequest: () -> Unit,
    deleteReminder: () -> Unit,
) {
    DefaultDialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Theme.colors.singleTheme)
                .padding(it),
        ) {
            TextForThisTheme(text = "Are you sure to delete reminder")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { onDismissRequest() }) {
                    TextForThisTheme(text = "no")
                }
                Button(onClick = {
                    deleteReminder()
                    onDismissRequest()
                }) {
                    TextForThisTheme(text = "ok")
                }
            }
        }
    }
}