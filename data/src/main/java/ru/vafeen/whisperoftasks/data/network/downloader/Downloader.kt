package ru.vafeen.whisperoftasks.data.network.downloader

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.vafeen.whisperoftasks.data.network.repository.NetworkRepository
import ru.vafeen.whisperoftasks.data.utils.pathToDownloadRelease
import java.io.File
import java.io.FileOutputStream


class Downloader(
    private val networkRepository: NetworkRepository
) {
    private val _percentageFlow = MutableSharedFlow<Float>()
    val percentageFlow = _percentageFlow.asSharedFlow()

    private val _isUpdateInProcessFlow = MutableSharedFlow<Boolean>()
    val isUpdateInProcessFlow = _isUpdateInProcessFlow.asSharedFlow()

    private fun installApk(context: Context) {
        val apkFilePath = context.pathToDownloadRelease()
        // Создаем объект File для APK файла по указанному пути
        val file = File(apkFilePath)

        // Проверяем, существует ли файл
        if (file.exists()) {
            // Создаем Intent для установки APK
            val intent = Intent(Intent.ACTION_VIEW).apply {
                // Устанавливаем URI и MIME-тип для файла
                setDataAndType(
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider", // Указываем авторитет FileProvider
                        file
                    ),
                    "application/vnd.android.package-archive" // MIME-тип для APK файлов
                )
                // Добавляем флаг для предоставления разрешения на чтение URI
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                // Добавляем флаг для запуска новой задачи
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            // Запускаем активность для установки APK
            context.startActivity(intent)
        } else {
            // Логируем ошибку, если файл не существует
            Log.e("InstallApk", "APK file does not exist: $apkFilePath")
        }
    }

    fun downloadApk(
        context: Context,
        url: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            _isUpdateInProcessFlow.emit(true)
        }
        val apkFilePath = context.pathToDownloadRelease()
        // Создаем вызов для загрузки файла
        val call = networkRepository.downloadFile(url)

        // Выполняем асинхронный запрос
        call?.enqueue(object : Callback<ResponseBody> {
            // Обрабатываем успешный ответ
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val body = response.body()
                        // Проверяем, успешен ли ответ
                        if (response.isSuccessful && body != null) {
                            // Создаем файл для записи данных
                            val file = File(apkFilePath)
                            // Получаем поток данных из тела ответа
                            val inputStream = body.byteStream()
                            // Создаем поток для записи данных в файл
                            val outputStream = FileOutputStream(file)
                            // Буфер для чтения данных
                            val buffer = ByteArray(8 * 1024)
                            var bytesRead: Int
                            var totalBytesRead: Long = 0
                            // Получаем длину содержимого
                            val contentLength = body.contentLength()

                            // Используем потоки для чтения и записи данных
                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                // запись данных из буфера в выходной поток
                                outputStream.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead
                                // Отправляем процент загрузки
                                _percentageFlow.emit(totalBytesRead.toFloat() / contentLength)
                                if (contentLength == totalBytesRead) {
                                    // отправляем окончание процесса загрузки
                                    _isUpdateInProcessFlow.emit(false)
                                    // установка
                                    installApk(context = context)
                                }
                            }
                        } else {
                            //  Отправляем сигнал о неудаче
                            _isUpdateInProcessFlow.emit(false)
                            _percentageFlow.emit(0f)
                        }
                    } catch (e: Exception) {
                        // Обрабатываем исключение и отправляем сигнал о неудаче
                        _isUpdateInProcessFlow.emit(false)
                        _percentageFlow.emit(0f)
                    }
                }
            }

            // Обрабатываем ошибку при выполнении запроса
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("status", "Download error: ${t.message}")
            }
        })
    }

}