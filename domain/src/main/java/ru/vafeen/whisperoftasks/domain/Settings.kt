package ru.vafeen.whisperoftasks.domain


import androidx.compose.ui.graphics.Color
import com.google.gson.Gson

data class Settings(
    val lightThemeColor: Color? = null,
    val darkThemeColor: Color? = null,
    val lastDemonstratedVersion: Int? = null,
    val catInSettings: Boolean = false,
) {
    fun toJsonString(): String = Gson().toJson(this)
}