package ru.vafeen.reminder.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.vafeen.reminder.ui.theme.FontSize
import ru.vafeen.reminder.ui.theme.Theme
import ru.vafeen.reminder.utils.getDateString
import ru.vafeen.reminder.utils.getTimeDefaultStr
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


@Composable
fun Border(itemHeight: Dp) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight)
            .background(Theme.colors.oppositeTheme)
            .padding(top = 2.dp, bottom = 2.dp)
            .background(Theme.colors.singleTheme)
    ) {}
}


@Composable
fun MyTimePicker(
    initialTime: LocalTime,
    onDateTimeSelected: (LocalDateTime) -> Unit,
) {
    val dAndTToDt = { d: LocalDate, t: LocalTime -> LocalDateTime.of(d, t) }
    var pickedTime by remember {
        mutableStateOf(
            LocalTime.of(
                initialTime.hour, initialTime.minute
            )
        )
    }
    var pickedDate by remember {
        mutableStateOf(LocalDate.now())
    }
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        DateColumnPicker(modifier = Modifier.weight(1f), onValueChange = {
            if (it != null) {
                pickedDate = it
                onDateTimeSelected(dAndTToDt(pickedDate, pickedTime))
            }
        })
        TimeColumnPicker(
            modifier = Modifier.weight(2f),
            value = pickedTime.hour,
            onValueChange = {
                if (it != null) {
                    pickedTime = LocalTime.of(it, pickedTime.minute)
                    onDateTimeSelected(dAndTToDt(pickedDate, pickedTime))
                }
            },
            range = 0..23,
        )
        TimeColumnPicker(
            value = pickedTime.minute, onValueChange = {
                if (it != null) {
                    pickedTime = LocalTime.of(pickedTime.hour, it)
                    onDateTimeSelected(dAndTToDt(pickedDate, pickedTime))
                }
            }, range = 0..59, modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DateColumnPicker(
    onValueChange: (LocalDate?) -> Unit, modifier: Modifier = Modifier
) {
    // Высота одного элемента
    val itemHeight = 40.dp
    // Количество видимых элементов в столбце
    val size = 3
    // Отступ между элементами
    val space = 24.dp
    // Высота списка (должна вмещать ровно три элемента)
    val listHeight = itemHeight * size + space * 2
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = 0)
    val dateToday = LocalDate.now()
    val list = mutableListOf("")
    for (i in 0..365) {
        list.add(dateToday.plusDays((i).toLong()).getDateString())
    }
    list.add("")
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            // Перемотка к центральному элементу
            listState.animateScrollToItem(listState.firstVisibleItemIndex)
        }
    }
    Box(
        modifier = modifier.height(listHeight), contentAlignment = Alignment.Center
    ) {

        Border(itemHeight = itemHeight)


        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(space)
        ) {
            itemsIndexed(list) { index, it ->
                val isSelected = listState.firstVisibleItemIndex == index - 1
                val newDT = dateToday.plusDays(index.toLong() - 1)
                if (isSelected) onValueChange(newDT)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextForThisTheme(
                        text = it,
                        fontSize = FontSize.medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeColumnPicker(
    value: Int, onValueChange: (Int?) -> Unit, range: IntRange, modifier: Modifier = Modifier
) {
    // Высота одного элемента
    val itemHeight = 40.dp
    // Количество видимых элементов в столбце
    val size = 3
    // Отступ между элементами
    val space = 24.dp
    // Высота списка (должна вмещать ровно три элемента)
    val listHeight = itemHeight * size + space * 2
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = value)

    val list = mutableListOf("")
    for (i in range) list.add(i.getTimeDefaultStr())
    list.add("")

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            // Перемотка к центральному элементу
            listState.animateScrollToItem(listState.firstVisibleItemIndex)
        }
    }

    Box(
        modifier = modifier.height(listHeight), contentAlignment = Alignment.Center
    ) {
        Border(itemHeight = itemHeight)

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(space)
        ) {
            itemsIndexed(list) { index, it ->
                val isSelected = listState.firstVisibleItemIndex == index - 1
                if (isSelected) onValueChange(list[listState.firstVisibleItemIndex + 1].toIntOrNull())
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextForThisTheme(text = it, fontSize = FontSize.medium)
                }
            }
        }
    }
}


