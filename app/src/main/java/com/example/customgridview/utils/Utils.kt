package com.example.customgridview.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.File
import java.io.IOException

@RequiresApi(Build.VERSION_CODES.KITKAT)
fun getImageUri(context : Context,image : Bitmap) : Uri {
    val relativeLocation = Environment.DIRECTORY_PICTURES + "/customgrid"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis().toString())
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    try {

        uri?.let { uri ->
            val stream = resolver.openOutputStream(uri)

            stream?.let { stream ->
                if (!image.compress(Bitmap.CompressFormat.JPEG, 100, stream)) {
                    throw IOException("Failed to save bitmap.")
                }
            } ?: throw IOException("Failed to get output stream.")

        } ?: throw IOException("Failed to create new MediaStore record")

    } catch (e: IOException) {
        if (uri != null) {
            resolver.delete(uri, null, null)
        }
        throw IOException(e)
    } finally {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
    }
    resolver.update(uri!!, contentValues, null, null)
    return uri
}