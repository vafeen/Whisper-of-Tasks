package ru.vafeen.whisperoftasks.ui.common.components

import androidx.compose.ui.graphics.Color
import com.google.gson.Gson

data class Settings(
    val lightThemeColor: Color? = null,
    val darkThemeColor: Color? = null,
) {
    fun toJsonString(): String = Gson().toJson(this)
}
