package ru.vafeen.whisperoftasks.ui.common.components.ui_utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import ru.vafeen.whisperoftasks.ui.theme.Theme

@Composable
fun TextForThisTheme(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null,
) {
    androidx.compose.material3.Text(
        text = text,
        modifier = modifier,
        color = Theme.colors.oppositeTheme,
        fontSize = fontSize,
        textAlign = textAlign
    )
}

@Composable
fun TextForOppositeTheme(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
) {
    androidx.compose.material3.Text(
        text = text,
        modifier = modifier,
        color = Theme.colors.singleTheme,
        fontSize = fontSize,
    )
}