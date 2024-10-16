package ru.vafeen.whisperoftasks.ui.common.screen

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.vafeen.whisperoftasks.R
import ru.vafeen.whisperoftasks.network.downloader.Downloader
import ru.vafeen.whisperoftasks.network.downloader.Progress
import ru.vafeen.whisperoftasks.noui.duration.RepeatDuration
import ru.vafeen.whisperoftasks.noui.local_database.entity.Reminder
import ru.vafeen.whisperoftasks.ui.common.components.bottom_bar.BottomBar
import ru.vafeen.whisperoftasks.ui.common.components.ui_utils.DeleteReminders
import ru.vafeen.whisperoftasks.ui.common.components.ui_utils.ReminderDataString
import ru.vafeen.whisperoftasks.ui.common.components.ui_utils.ReminderDialog
import ru.vafeen.whisperoftasks.ui.common.components.ui_utils.TextForThisTheme
import ru.vafeen.whisperoftasks.ui.common.components.ui_utils.UpdateProgress
import ru.vafeen.whisperoftasks.ui.common.navigation.ScreenRoute
import ru.vafeen.whisperoftasks.ui.common.viewmodel.MainScreenViewModel
import ru.vafeen.whisperoftasks.ui.theme.FontSize
import ru.vafeen.whisperoftasks.ui.theme.Theme
import ru.vafeen.whisperoftasks.utils.Path
import ru.vafeen.whisperoftasks.utils.getDateStringWithWeekOfDay
import ru.vafeen.whisperoftasks.utils.getMainColorForThisTheme
import ru.vafeen.whisperoftasks.utils.suitableColor
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    navController: NavController, viewModel: MainScreenViewModel,
) {
    val context = LocalContext.current
    val defaultColor = Theme.colors.mainColor
    val progress = remember {
        mutableStateOf(Progress(totalBytesRead = 0L, contentLength = 0L, done = false))
    }
    val dark = isSystemInDarkTheme()
    val mainColor by remember {
        mutableStateOf(
            viewModel.settings.getMainColorForThisTheme(isDark = dark) ?: defaultColor
        )
    }
    var isUpdateInProcess by remember {
        mutableStateOf(false)
    }
    val cor = rememberCoroutineScope()
    var localTime by remember {
        mutableStateOf(LocalTime.now())
    }
    var localDate by remember {
        mutableStateOf(LocalDate.now())
    }
    var isEditingReminder by remember {
        mutableStateOf(false)
    }
    val lastReminder: MutableState<Reminder?> = remember {
        mutableStateOf(null)
    }
    var isDeletingInProcess by remember { mutableStateOf(false) }
    val reminderForRemoving = remember { mutableStateMapOf<Int, Reminder>() }
    fun Modifier.combinedClickableForRemovingReminder(reminder: Reminder): Modifier =
        this.combinedClickable(
            onClick = {
                if (!isDeletingInProcess) {
                    lastReminder.value = reminder
                    isEditingReminder = true
                } else {
                    if (reminderForRemoving[reminder.idOfReminder] == null)
                        reminderForRemoving[reminder.idOfReminder] = reminder
                    else {
                        reminderForRemoving.remove(reminder.idOfReminder)
                        if (reminderForRemoving.isEmpty())
                            isDeletingInProcess = false
                    }
                }
            },
            onLongClick = {
                isDeletingInProcess = !isDeletingInProcess
                if (isDeletingInProcess)
                    reminderForRemoving[reminder.idOfReminder] = reminder
                else
                    reminderForRemoving.clear()
            }
        )

    fun Reminder.isItCandidateForDelete(): Boolean? =
        if (isDeletingInProcess) reminderForRemoving[idOfReminder] != null else null

    fun Reminder.changeStatusOfDeleting() {
        isItCandidateForDelete()?.let {
            if (it)
                reminderForRemoving.remove(idOfReminder)
            else reminderForRemoving.set(idOfReminder, this)
        }
    }
    LaunchedEffect(key1 = null) {
        Downloader.isUpdateInProcessFlow.collect {
            isUpdateInProcess = it
        }
    }
    LaunchedEffect(key1 = null) {
        Downloader.sizeFlow.collect {
            if (!it.failed) {
                progress.value = it
                if (it.contentLength == it.totalBytesRead) {
                    isUpdateInProcess = false
                    Downloader.installApk(
                        context = context, apkFilePath = Path.path(context).toString()
                    )
                }
            } else isUpdateInProcess = false
        }
    }

    val reminders by viewModel.remindersFlow.collectAsState(listOf())
    val cardsWithDateState = rememberLazyListState()

    val pagerState = rememberPagerState(
        pageCount = {
            viewModel.pageNumber
        }, initialPage = 0
    )
    BackHandler {
        when {
            isDeletingInProcess -> {
                isDeletingInProcess = false
                reminderForRemoving.clear()
            }

            !isDeletingInProcess && pagerState.currentPage == 0 -> {
                navController.popBackStack()
                (context as Activity).finish()
            }

            else -> {
                cor.launch(Dispatchers.Main) {
                    pagerState.animateScrollToPage(0)
                }
            }
        }
    }

    LaunchedEffect(key1 = null) {
        withContext(Dispatchers.Main) {
            while (true) {
                localTime = LocalTime.now()
                delay(timeMillis = 1000L)
            }
        }
    }
    LaunchedEffect(key1 = pagerState.currentPage) {
        cardsWithDateState.animateScrollToItem(
            if (pagerState.currentPage > 0) pagerState.currentPage - 1
            else pagerState.currentPage
        )
    }
    Scaffold(containerColor = Theme.colors.singleTheme,
        bottomBar = {
            BottomBar(
                enabled = !isUpdateInProcess,
                containerColor = Theme.colors.mainColor,
                selectedMainScreen = true,
                navigateToRemindersScreen = {
                    if (!isUpdateInProcess) navController.navigate(
                        ScreenRoute.Reminders.route
                    )
                },
                navigateToSettingsScreen = {
                    if (!isUpdateInProcess) navController.navigate(
                        ScreenRoute.Settings.route
                    )
                })
        },
        topBar = {
            TopAppBar(colors = TopAppBarColors(
                containerColor = Theme.colors.singleTheme,
                scrolledContainerColor = Theme.colors.singleTheme,
                navigationIconContentColor = Theme.colors.oppositeTheme,
                titleContentColor = Theme.colors.oppositeTheme,
                actionIconContentColor = Theme.colors.singleTheme
            ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextForThisTheme(
                            text = stringResource(id = R.string.tasks_by_day),
                            fontSize = FontSize.huge27
                        )
                    }

                })

        }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Theme.colors.singleTheme)
        ) {
            if (isEditingReminder) {
                ReminderDialog(
                    newReminder = lastReminder as MutableState<Reminder>, // safety is above
                    onDismissRequest = { isEditingReminder = false })
            }
            LazyRow(
                state = cardsWithDateState,
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                items(count = viewModel.pageNumber) { index ->
                    val day = viewModel.todayDate.plusDays(index.toLong())
                    Card(modifier = Modifier
                        .fillParentMaxWidth(1 / 3f)
                        .padding(horizontal = 5.dp)
                        .clickable {
                            cor.launch(Dispatchers.Main) {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                        .alpha(
                            if (day != localDate && day != viewModel.todayDate) 0.5f else 1f
                        ), colors = CardDefaults.cardColors(
                        containerColor = if (day == viewModel.todayDate) mainColor
                        else Theme.colors.buttonColor,
                        contentColor = (if (day == viewModel.todayDate) mainColor
                        else Theme.colors.buttonColor).suitableColor()
                    )) {
                        Text(
                            text = day.getDateStringWithWeekOfDay(context = context),
                            fontSize = FontSize.small17,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical = 5.dp, horizontal = 10.dp
                                ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(10f)
                    .padding(top = 10.dp),
            ) { page ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    val dateOfThisPage = viewModel.todayDate.plusDays(page.toLong())
                    localDate = viewModel.todayDate.plusDays(pagerState.currentPage.toLong())
                    val remindersForThisDay = reminders.filter {
                        val d = it.dt.toLocalDate()
                        d == dateOfThisPage ||
                                it.repeatDuration == RepeatDuration.EveryDay && d <= dateOfThisPage ||
                                it.repeatDuration == RepeatDuration.EveryWeek && it.dt.dayOfWeek == dateOfThisPage.dayOfWeek
                    }
                    val lostReminders = reminders.filter {
                        it.repeatDuration == RepeatDuration.NoRepeat &&
                                dateOfThisPage > it.dt.toLocalDate()
                    }
                    if (remindersForThisDay.isEmpty() && lostReminders.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            TextForThisTheme(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                text = stringResource(id = R.string.no_events_today),
                                fontSize = FontSize.big22
                            )
                        }
                    }

                    if (remindersForThisDay.isNotEmpty()) {
                        remindersForThisDay.forEach {
                            it.ReminderDataString(
                                modifier = Modifier.combinedClickableForRemovingReminder(reminder = it),
                                viewModel = viewModel,
                                dateOfThisPage = dateOfThisPage,
                                context = context,
                                isItCandidateForDelete = it.isItCandidateForDelete(),
                                changeStatusOfDeleting = it::changeStatusOfDeleting,
                            )
                        }
                    }

                    if (lostReminders.isNotEmpty()) {
                        TextForThisTheme(
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            text = stringResource(id = R.string.past_events),
                            fontSize = FontSize.medium19
                        )
                        lostReminders.forEach {
                            it.ReminderDataString(
                                modifier = Modifier.combinedClickableForRemovingReminder(reminder = it),
                                viewModel = viewModel,
                                dateOfThisPage = dateOfThisPage,
                                context = context,
                                isItCandidateForDelete = it.isItCandidateForDelete(),
                                changeStatusOfDeleting = it::changeStatusOfDeleting,
                            )
                        }
                    }
                }
            }
            if (isDeletingInProcess) DeleteReminders {
                isDeletingInProcess = false
                reminderForRemoving.values.forEach {
                    viewModel.removeEvent(it)
                    reminderForRemoving.remove(it.idOfReminder)
                }
            }
            if (isUpdateInProcess) UpdateProgress(percentage = progress)
        }
    }
}
