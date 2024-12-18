package ru.vafeen.whisperoftasks.data.utils

import java.util.Random

fun List<Int>.generateID(): Int {
    var newID: Int
    do {
        newID = Random().nextInt()
    } while (newID in this || newID < 0)
    return newID
}
