package ru.vafeen.whisperoftasks.domain.usecase

import ru.vafeen.whisperoftasks.domain.local_database.ReminderRepository
import ru.vafeen.whisperoftasks.domain.models.Reminder

class DeleteAllRemindersUseCase(private val reminderRepository: ReminderRepository) {
    suspend operator fun invoke(vararg reminder: Reminder) =
        reminderRepository.deleteAllReminders(entity = reminder)
}