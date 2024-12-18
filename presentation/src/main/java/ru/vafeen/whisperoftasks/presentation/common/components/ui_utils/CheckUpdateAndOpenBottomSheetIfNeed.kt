package ru.vafeen.whisperoftasks.presentation.common.components.ui_utils

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import ru.vafeen.whisperoftasks.data.network.repository.NetworkRepository
import ru.vafeen.whisperoftasks.data.utils.getVersionName
import ru.vafeen.whisperoftasks.network.parcelable.github_service.Release
import ru.vafeen.whisperoftasks.presentation.ui.common.components.bottom_sheet.UpdaterBottomSheet


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckUpdateAndOpenBottomSheetIfNeed(
    networkRepository: NetworkRepository,
    onDismissRequest: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val versionName = getVersionName(context = context)
    val bottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isUpdateNeeded by remember {
        mutableStateOf(false)
    }
    var release: Release? by remember {
        mutableStateOf(null)
    }
    LaunchedEffect(key1 = null) {
        release = networkRepository.getLatestRelease()?.body()
        if (release != null && versionName != null &&
            release?.tag_name?.substringAfter("v") != versionName
        )
            isUpdateNeeded = true
    }

    if (isUpdateNeeded)
        release?.let { releaseParam ->
            UpdaterBottomSheet(
                release = releaseParam,
                state = bottomSheetState,
            ) {
                isUpdateNeeded = false
                onDismissRequest(it)
            }
        }
}