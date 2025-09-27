package com.example.filetransfer

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okio.BufferedSink
import okio.buffer
import okio.source
import java.io.InputStream

class HomeViewModel : ViewModel() {

    // Upload progress için LiveData
    private val _uploadProgress = MutableLiveData<Int>()
    val uploadProgress: LiveData<Int> get() = _uploadProgress

    private val _uploadStatus = MutableLiveData<String>()
    val uploadStatus: LiveData<String> get() = _uploadStatus

    fun sendFile(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch(Dispatchers.IO) {
            val inputStream = contentResolver.openInputStream(uri)
            inputStream?.let {
                val fileName = getFileName(uri, contentResolver)
                uploadFileToServer(it, fileName)
            } ?: run {
                _uploadStatus.postValue("Dosya açılamadı")
            }
        }
    }

    private fun getFileName(uri: Uri, contentResolver: ContentResolver): String {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex("_display_name")
            cursor.moveToFirst()
            return cursor.getString(nameIndex)
        }
        return "upload_file"
    }

    private fun uploadFileToServer(stream: InputStream, fileName: String) {
        val client = OkHttpClient()

        // Progress destekli RequestBody
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                fileName,
                object : RequestBody() {
                    override fun contentType() = "application/octet-stream".toMediaType()

                    override fun writeTo(sink: BufferedSink) {
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var uploaded = 0L
                        val total = stream.available().toLong()
                        stream.use { input ->
                            var read: Int
                            while (input.read(buffer).also { read = it } != -1) {
                                sink.write(buffer, 0, read)
                                uploaded += read
                                if (total > 0) {
                                    val progress = ((uploaded * 100) / total).toInt()
                                    _uploadProgress.postValue(progress)
                                }
                            }
                        }
                    }
                }
            )
            .build()

        val request = Request.Builder()
            .url("http://10.0.2.2:5000/upload")
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    _uploadStatus.postValue("Upload failed: ${response.code}")
                } else {
                    _uploadStatus.postValue("Upload successful!")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _uploadStatus.postValue("Upload error: ${e.message}")
        }
    }
}
