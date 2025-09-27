package com.example.filetransfer.api
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

fun sendFileToServer(file: File) {
    val client = OkHttpClient()

    // application/octet-stream ile dosya request body
    val requestBody = file.asRequestBody("application/octet-stream".toMediaType())

    val request = Request.Builder()
        .url(" http://192.168.1.178:5000/upload")
        .post(requestBody)
        .build()

    client.newCall(request).execute().use { response: Response ->
        if (!response.isSuccessful) {
            throw Exception("Upload failed: ${response.code}")
        }
        println("Server response: ${response.body?.string()}")
    }
}
